package com.example.broadcastsos.network

import com.github.scribejava.core.model.OAuth1RequestToken

object Network {

    sealed class Result {
        class NetworkError(val message: String) : Result()
        class NetworkResult(val authUrl: String, val requestToken: String, val requestTokenSecret: String) : Result()
        class NetworkResult2(val accessToken: String, val accessTokenSecret: String) : Result()
    }
}
