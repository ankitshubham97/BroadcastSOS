package com.example.broadcastsos.services.twitter.rest

import android.content.Context
import android.util.Log

interface ITwitterApis {
    fun sendTweet(context: Context, message: String)
    fun getFollowers(context: Context, requestCode: String)
    fun sendDM(context: Context, recipientId: String, msg: String, requestCode: String)
}
