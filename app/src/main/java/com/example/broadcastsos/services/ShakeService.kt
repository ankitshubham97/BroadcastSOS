package com.example.broadcastsos.services

//import android.support.v4.app.NotificationCompat
import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager


class ShakeService : Service(), SensorEventListener {

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mAccel: Float = 0.toFloat() // acceleration apart from gravity
    private var mAccelCurrent: Float = 0.toFloat() // current acceleration including gravity
    private var mAccelLast: Float = 0.toFloat() // last acceleration including gravity
    private val twitterService: TwitterService by lazy { TwitterService() }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager!!.registerListener(
            this, mAccelerometer,
            SensorManager.SENSOR_DELAY_UI, Handler()
        )
        return Service.START_STICKY
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
            if (sendTweet) {
                val defaultMsg = "SOS: This is an auto-generated message whenever I am in danger. Please help me!"
                var msg = sharedPreferences.getString("settings_sos_message", defaultMsg) ?: defaultMsg
                val sendLocation = sharedPreferences.getBoolean("settings_enable_sending_location_in_tweet", true)
                val sendCloseContacts = sharedPreferences.getBoolean("settings_enable_sending_close_contacts_in_tweet", true)
                // TODO: implement close contacts feature.

                if (sendLocation) {
                    val location = getLocation()
                    if (location != null) {
                        msg += " My location: https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                    }
                }
                twitterService.sendTweet(this, msg)
            }
        }
    }

    fun getLocation() : Location? {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null
        }
        val locationGPS: Location? = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (locationGPS != null) {
            return locationGPS
        } else {
            Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show()
        }
        return null
    }

}
