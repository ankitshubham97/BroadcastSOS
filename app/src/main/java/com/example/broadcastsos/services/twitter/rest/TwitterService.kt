package com.example.broadcastsos.services.twitter.rest

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException


class TwitterService(private val view: MainView) : NetworkAccess {
    private val client = OkHttpClient();
    private lateinit var sharedPref: SharedPreferences;
    private var coroutineScope: CoroutineScope? = null

    private val errorHandler = CoroutineExceptionHandler { context, error ->
        logOut("Async Exception")
        coroutineScope?.launch(Dispatchers.Main) {
            logOut("Async Exception Result")
            error.printStackTrace()
            logOut(error.message?: error.toString())
            view.updateScreen(error.localizedMessage ?: "")
        }
    }
    private fun getOauthKeys(context: Context) =
        OauthKeys(
            consumerKey = "Aybi4VfPWujGV6RBAope2Y23k",
            consumerSecret = "dT8dVSAImrX2ByaTLKG5tgkCRqoNHRlkHRNhxG5Z2MfztdGd5j",
            accessToken = sharedPref.getString("accessToken", ""),
            accessSecret = sharedPref.getString("accessTokenSecret", "")
        )

    override fun sendTweet(context: Context, tweet: String) {

        coroutineScope?.cancel()
        coroutineScope = MainScope()
        coroutineScope?.launch(errorHandler) {
            try {
                val defer = async(Dispatchers.IO) {
                    logOut("Async Fetch Started")
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

                    Network.fetchHttpResult(signed).apply {
                        logOut("Async Fetch Done")
                    }
                }
                when (val result = defer.await()) {
                    is Response -> {
                        logOut("Async Fetch Result")
                        view.updateScreen("Tweeted")
                    }
                }
            } catch (e: CancellationException) {
                logOut("Async Cancel Result")
            }
        }

    }

    override fun getFollowers(context: Context, requestCode: String) {
        coroutineScope?.cancel()
        coroutineScope = MainScope()
        coroutineScope?.launch(errorHandler) {
            try {
                val defer = async(Dispatchers.IO) {
                    logOut("Async Fetch Started")
                    sharedPref = context.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
                    val oauthKeys = getOauthKeys(context)
                    val oauth1 = Oauth1SigningInterceptor(oauthKeys = oauthKeys)
                    val userId = sharedPref.getString("userId", "") ?: ""
                    Log.i("userId", userId)
                    val request = Request.Builder()
                        .url("https://api.twitter.com/2/users/${userId}/followers")
                        .get()
                        .build()

                    val signed = oauth1.signRequest(request)

                    Network.fetchHttpResult(signed).apply {
                        logOut("Async Fetch Done")
                    }
                }
                when (val result = defer.await()) {
                    is Response -> {
                        val body = result.body()?.string()
                        Log.i("code", result.code().toString())
                        Log.i("body", body ?: "")
                        view.updateScreen(body ?: "")
                        view.fetchResponse(body ?: "", result.code(),  requestCode)
                        logOut("Async Post Success Result")
                    }
                }
            } catch (e: CancellationException) {
                logOut("Async Cancel Result")
            }
        }
    }

    override fun fetchData(httpUrlBuilder: HttpUrl.Builder, searchText: String) {
        TODO("Not yet implemented")
    }

    override fun terminate() {
        TODO("Not yet implemented")
    }

}