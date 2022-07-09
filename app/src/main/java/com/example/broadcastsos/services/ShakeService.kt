package com.example.broadcastsos.services

//import android.support.v4.app.NotificationCompat
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.*
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


class ShakeService : Service(), SensorEventListener {

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mAccel: Float = 0.toFloat() // acceleration apart from gravity
    private var mAccelCurrent: Float = 0.toFloat() // current acceleration including gravity
    private var mAccelLast: Float = 0.toFloat() // last acceleration including gravity

    private val client = OkHttpClient()


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
            val request = Request.Builder()
                .url("https://enqpcnmttxmgb.x.pipedream.net/")
                .post(
                    FormBody.Builder()
                        .add("from", "app")
                        .add("time", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                        .build())
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
            })
        }
    }

}
