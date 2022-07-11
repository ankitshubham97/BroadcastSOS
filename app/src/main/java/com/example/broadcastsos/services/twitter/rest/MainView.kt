package com.example.broadcastsos.services.twitter.rest

import okhttp3.Response

interface MainView {
    fun updateScreen(result: String)
    fun fetchResponse(responseBody: String, responseCode: Int, requestCode: String)
}
