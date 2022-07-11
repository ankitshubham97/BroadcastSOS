package com.example.broadcastsos.models


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.broadcastsos.services.ShakeService
import com.example.broadcastsos.services.RestarterBroadcastReceiver
import kotlinx.coroutines.CoroutineScope


class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application
    // this will track the value of the counter in the Service and report it to the MainActivity
    var currentCounter: MutableLiveData<Int> = ShakeService.counter

    fun startCounter() {
        RestarterBroadcastReceiver.startWorker(context)
    }

}
