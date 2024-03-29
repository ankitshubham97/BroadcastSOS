package com.example.broadcastsos.services.background


import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.broadcastsos.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ShakeCoroutineWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object {
        private var TAG = this::class.simpleName
        private var NOTIFICATION_ID = 9973
    }

    override suspend fun doWork(): Result {
        // do not launch if the service is already alive
        if (ShakeService.currentService == null) {
            withContext(Dispatchers.IO) {
                val trackerServiceIntent = Intent(context, ShakeService::class.java)
                ServiceNotification.notificationText = "do not close the app, please"
                ServiceNotification.notificationIcon = R.drawable.ic_launcher_foreground
                Log.i(TAG, "Launching tracker")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(trackerServiceIntent)
                } else {
                    context.startService(trackerServiceIntent)
                }
            }
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        ServiceNotification.notificationText = "do not close the app, please"
        ServiceNotification.notificationIcon = R.drawable.ic_launcher_foreground
        val notification = ServiceNotification(context, NOTIFICATION_ID, true)
        return ForegroundInfo(NOTIFICATION_ID, notification.notification!!)
    }
}
