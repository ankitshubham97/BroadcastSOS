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
import com.example.broadcastsos.Constants.Companion.DELETE_TWEET_INTENT_PAYLOAD
import com.example.broadcastsos.Constants.Companion.GET_BROADCASTSOS_TWEETS
import com.example.broadcastsos.Constants.Companion.SEND_DM
import com.example.broadcastsos.Constants.Companion.SEND_TWEET
import com.example.broadcastsos.MainActivity
import com.example.broadcastsos.R
import com.example.broadcastsos.services.twitter.rest.Oauth1SigningInterceptor
import com.example.broadcastsos.services.twitter.rest.TwitterViewModel
import com.example.broadcastsos.services.twitter.rest.TwitterService
import com.example.broadcastsos.services.twitter.rest.models.CreateTweetResponseModel
import com.example.broadcastsos.services.twitter.rest.models.GetFollowersResponseModel
import com.example.broadcastsos.services.twitter.rest.models.GetTweetsResponseModel
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

        if (mAccel < 11) {
            return
        }
        Log.i("TAG","Shaken!!!!")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
        val sendTweet = sharedPreferences.getBoolean("settings_enable_sending_tweet", false)
        val defaultMsg = "SOS: This is an auto-generated message whenever I am in danger. Please help me!"
        var msg = "#BroadcastSOS ${sharedPreferences.getString("settings_sos_message", defaultMsg) ?: defaultMsg}"
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
        if (requestCode == GET_BROADCASTSOS_TWEETS) {
            if (responseCode == 200) {
                val result =
                    Gson().fromJson(responseBody, GetTweetsResponseModel::class.java)
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                intent.putExtra(DELETE_TWEET_INTENT_PAYLOAD, result.data[0].id);
                val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
                val timeToUndo = sharedPreferences.getString("settings_time_to_undo_false_alarm", "60" /* 1 minute */)!!.toLong()*1000
               //{"errors":[{"parameters":{"tweet.fields":["1547375481640521728"]},"message":"The `tweet.fields` query parameter value [1547375481640521728] is not one of [attachments,author_id,context_annotations,conversation_id,created_at,entities,geo,id,in_reply_to_user_id,lang,non_public_metrics,organic_metrics,possibly_sensitive,promoted_metrics,public_metrics,referenced_tweets,reply_settings,source,text,withheld]"}],"title":"Invalid Request","detail":"One or more parameters to your request was invalid.","type":"https://api.twitter.com/2/problems/invalid-request"}

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
            }
        } else if (requestCode == SEND_TWEET) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
            val sharedPref = this.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
            if (responseCode != 201) {
                Log.i(TAG, "Error sending tweet: $responseBody")
                // Mostly this is due to the fact that multiple tweets with the same content are sent at the same time. Let's retrieve the last successful tweet and pass that in the following intent.
                twitterService.getBroadcastSosTweets(this, GET_BROADCASTSOS_TWEETS)
                return
            }
            Log.i(TAG, "Tweet sent")
            val result =
                Gson().fromJson(responseBody, CreateTweetResponseModel::class.java)
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            Log.i(TAG, "Tweet sent: ${result.data.id}")
            intent.putExtra(DELETE_TWEET_INTENT_PAYLOAD, result.data.id);
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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