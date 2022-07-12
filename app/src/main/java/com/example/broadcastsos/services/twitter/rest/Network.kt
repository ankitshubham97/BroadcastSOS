package com.example.broadcastsos.services.twitter.rest

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

object Network {
    private val httpClient = OkHttpClient
            .Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .build()

    fun fetchHttpResult(request: Request): Response {
        return httpClient.newCall(request).execute()
    }

}
