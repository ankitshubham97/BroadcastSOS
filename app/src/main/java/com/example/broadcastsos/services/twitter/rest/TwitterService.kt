package com.example.broadcastsos.services.twitter.rest

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
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
        sharedPref = context.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        val oauthKeys = getOauthKeys(context)
        val oauth1 = Oauth1SigningInterceptor(oauthKeys = oauthKeys)

        val mediaTypeJson = MediaType.parse("application/json; charset=utf-8")

        val body: RequestBody = RequestBody.create(mediaTypeJson, "{\"text\":\"$tweet\"}")

        val request = Request.Builder()
            .url("https://api.twitter.com/2/tweets")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val signed = oauth1.signRequest(request)

        client.newCall(signed).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
        })
    }

}