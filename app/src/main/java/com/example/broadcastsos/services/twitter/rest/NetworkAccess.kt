package com.example.broadcastsos.services.twitter.rest

import android.util.Log
import okhttp3.HttpUrl

interface NetworkAccess {
    fun fetchData(httpUrlBuilder: HttpUrl.Builder, searchText: String)
    fun terminate()
    fun logOut(message: String) {
        Log.d("Track", "$message ${Thread.currentThread()}")
    }
}
