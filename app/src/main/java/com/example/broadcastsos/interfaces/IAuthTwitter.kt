package com.example.broadcastsos.interfaces

interface IAuthTwitter {
    fun saveOauthTokenAndUpdateAuthUrl(oauthToken: String, oauthTokenSecret: String, authUrl: String)
    fun errorOnSavingOauthTokenOrUpdatingAuthUrl()

    fun saveAccessToken(accessToken: String, accessTokenSecret: String)
    fun errorOnVerifyingToken()
}
