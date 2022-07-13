/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */
package com.example.broadcastsos.services.background

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import com.example.broadcastsos.Constants
import com.example.broadcastsos.Constants.Companion.CHANNEL_ID
import com.example.broadcastsos.Constants.Companion.SEND_DM
import com.example.broadcastsos.Constants.Companion.SEND_TWEET
import com.example.broadcastsos.MainActivity
import com.example.broadcastsos.R
import com.example.broadcastsos.services.twitter.rest.TwitterViewModel
import com.example.broadcastsos.services.twitter.rest.TwitterService
import com.example.broadcastsos.services.twitter.rest.models.GetFollowersResponseModel
import com.google.gson.Gson

class ShakeService : Service(), SensorEventListener, TwitterViewModel {
    private var wakeLock: WakeLock? = null
    private var currentServiceNotification: ServiceNotification? = null
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mAccel: Float = 0.toFloat() // acceleration apart from gravity
    private var mAccelCurrent: Float = 0.toFloat() // current acceleration including gravity
    private var mAccelLast: Float = 0.toFloat() // last acceleration including gravity
    private val twitterService: TwitterService by lazy { TwitterService(this) }

    companion object {
        var currentService: ShakeService? = null

        private val TAG = ShakeService::class.java.simpleName
        private const val NOTIFICATION_ID = 9974
    }

    override fun onCreate() {
        super.onCreate()
        currentService = this
    }


    /**
     * it starts the foreground process
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "starting the foreground service...")
        startWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Log.i(TAG, "starting foreground process")
                currentServiceNotification = ServiceNotification(this, NOTIFICATION_ID, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, currentServiceNotification!!.notification!!, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                } else {
                    startForeground(NOTIFICATION_ID, currentServiceNotification!!.notification)
                }
                Log.i(TAG, "Starting foreground process successful!")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground process " + e.message)
            }
        }

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager!!.registerListener(
            this, mAccelerometer,
            SensorManager.SENSOR_DELAY_UI, Handler()
        )
        return START_STICKY

    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(TAG, "on bind")
        return null
    }

    /**
     * it acquires the wakelock
     */
    private fun startWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        // you must acquire a wake lock in order to keep the service going
        // android studio will complain that it does not like the wake lock not to have an ending time
        // but that is exactly what we need a permanent wake lock - we are implementing a never
        // ending service!
        wakeLock?.acquire()
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        mAccelLast = mAccelCurrent
        mAccelCurrent = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = mAccelCurrent - mAccelLast
        mAccel = mAccel * 0.9f + delta // perform low-cut filter

        if (mAccel > 11) {
            Log.i("TAG","Shaken!!!!")
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
            val sendTweet = sharedPreferences.getBoolean("settings_enable_sending_tweet", false)
            val defaultMsg = "SOS: This is an auto-generated message whenever I am in danger. Please help me!"
            var msg = sharedPreferences.getString("settings_sos_message", defaultMsg) ?: defaultMsg
            if (sendTweet) {
                val sendLocation = sharedPreferences.getBoolean("settings_enable_sending_location_in_tweet", true)
                if (sendLocation) {
                    Log.i(TAG, "Sending location enabled")
                    val location = getLocation()
                    if (location != null) {
                        Log.i(TAG, "Sending location")
                        msg += " My location: https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                    }
                }
                twitterService.sendTweet(this, msg, SEND_TWEET)
            }
            val sendDMsToCloseContacts = sharedPreferences.getBoolean("settings_enable_sending_dms_to_close_contacts", true)
            if (sendDMsToCloseContacts) {
                val closeContactIds = sharedPreferences.getStringSet("settings_close_contacts", null);
                if (closeContactIds != null) {
                    for (i in closeContactIds) {
                        twitterService.sendDM(this, i, msg, SEND_DM)
                    }
                }
            }
        }
    }

    private fun getLocation() : Location? {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Can't do much here, so just return null.
            return null
        }
        val locationGPS: Location? = locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        if (locationGPS != null) {
            return locationGPS
        }
        Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show()
        return null
    }

    override fun syncResponse(responseBody: String, responseCode: Int, requestCode: String) {
        if (requestCode == SEND_TWEET) {
            Log.i(TAG, "Tweet sent")
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
            val timeToUndo = sharedPreferences.getString("settings_time_to_undo_false_alarm", "60" /* 1 minute */)!!.toLong()*1000
            Log.i(TAG, "Time to undo: $timeToUndo")
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("SOS Sent!")
                .setContentText("Tap here to undo the SOS tweet.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setTimeoutAfter(timeToUndo)
                .setAutoCancel(true)
            with(NotificationManagerCompat.from(this)) {
                notify(121, builder.build())
            }
        } else if (requestCode == SEND_DM) {
            Log.i(TAG, "DM sent")
        }
    }

}