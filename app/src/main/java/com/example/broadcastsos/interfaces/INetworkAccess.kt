package com.example.broadcastsos.interfaces


import android.util.Log

interface INetworkAccess {
    fun fetchRequestTokenAndAuthUrl()
    fun terminate()
    fun logOut(message: String) {
        Log.d("Track", "$message ${Thread.currentThread()}")
    }
    fun verifyToken(oauthToken: String, oauthTokenSecret: String, oauthVerifier: String)
}
