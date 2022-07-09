package com.example.broadcastsos.interfaces


import android.util.Log
import com.github.scribejava.core.model.OAuth1RequestToken

interface INetworkAccess {
    fun fetchData()
    fun terminate()
    fun logOut(message: String) {
        Log.d("Track", "$message ${Thread.currentThread()}")
    }
    fun verifyToken(oauthToken: String, oauthTokenSecret: String, oauthVerifier: String)
}
