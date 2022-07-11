package com.example.broadcastsos.services

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.*
import java.io.IOException


class TwitterService {
    private val client = OkHttpClient();
    private lateinit var sharedPref: SharedPreferences;

    private fun getOauthKeys(context: Context) =
        OauthKeys(
            consumerKey = "Aybi4VfPWujGV6RBAope2Y23k",
            consumerSecret = "dT8dVSAImrX2ByaTLKG5tgkCRqoNHRlkHRNhxG5Z2MfztdGd5j",
            accessToken = sharedPref.getString("accessToken", ""),
            accessSecret = sharedPref.getString("accessTokenSecret", "")
        )

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendTweet(context: Context, tweet: String) {
//        val nonce = "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"
        sharedPref = context.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        val oauthKeys = getOauthKeys(context)
        Log.i("TwitterService", "oauthKeys: $oauthKeys")
        val oauth1 = Oauth1SigningInterceptor(oauthKeys = oauthKeys)

        val mediaTypeJson = MediaType.parse("application/json; charset=utf-8")

        val body: RequestBody = RequestBody.create(mediaTypeJson, "{\"text\":\"$tweet\"}")

        val request = Request.Builder()
            .url("https://api.twitter.com/2/tweets")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val signed = oauth1.signRequest(request)
        Log.i("Signed", signed.header("Authorization"))
        Log.i("Signed", signed.header("Content-Type"))
        Log.i("Signed", signed.headers().toString())
        // Send tweet
//        val request = Request.Builder()
//            .url("https://enqpcnmttxmgb.x.pipedream.net/")
//            .post(
//                FormBody.Builder()
//                    .add("from", "app")
//                    .add("time", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
//                    .build())
//            .build()
        client.newCall(signed).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
        })
    }

}