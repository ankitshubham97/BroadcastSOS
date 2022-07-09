package com.example.broadcastsos.interfaces

interface IAuthTwitter {
    fun updateAuthUrl(result: String)
    fun saveOauthToken(oauthToken: String, oauthTokenSecret: String)
    fun saveAccessToken(accessToken: String, accessTokenSecret: String)
}
