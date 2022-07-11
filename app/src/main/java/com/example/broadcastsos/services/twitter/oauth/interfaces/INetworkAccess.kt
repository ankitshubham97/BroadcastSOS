package com.example.broadcastsos.services.twitter.oauth.interfaces


import android.util.Log

interface INetworkAccess {
    fun fetchRequestTokenAndAuthUrl()
    fun terminate()
    fun logOut(message: String) {
        Log.d("Track", "$message ${Thread.currentThread()}")
    }
    fun verifyToken(oauthToken: String, oauthTokenSecret: String, oauthVerifier: String)
}
