package com.example.broadcastsos.models


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.broadcastsos.services.RestarterBroadcastReceiver


class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application

    fun startWorker() {
        RestarterBroadcastReceiver.startWorker(context)
    }

}
