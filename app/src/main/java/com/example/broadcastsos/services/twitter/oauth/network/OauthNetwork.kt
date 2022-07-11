package com.example.broadcastsos.services.twitter.oauth.network

object OauthNetwork {

    sealed class Result {
        class NetworkError(val message: String) : Result()
        class NetworkResult(val authUrl: String, val requestToken: String, val requestTokenSecret: String) : Result()
        class NetworkResult2(val accessToken: String, val accessTokenSecret: String) : Result()
    }
}
