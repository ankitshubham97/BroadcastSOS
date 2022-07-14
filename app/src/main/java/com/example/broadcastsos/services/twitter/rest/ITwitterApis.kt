package com.example.broadcastsos.services.twitter.rest

import android.content.Context
import android.util.Log

interface ITwitterApis {
    fun sendTweet(context: Context, message: String, requestCode: String)
    fun deleteTweet(context: Context, tweetId: String, requestCode: String)
    fun getBroadcastSosTweets(context: Context, requestCode: String)
    fun getFollowers(context: Context, requestCode: String)
    fun sendDM(context: Context, recipientId: String, msg: String, requestCode: String)
    fun getMe(context: Context, requestCode: String)
}
