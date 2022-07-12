package com.example.broadcastsos.services.twitter.rest

interface TwitterViewModel {
    fun syncResponse(responseBody: String, responseCode: Int, requestCode: String)
}
