package com.example.broadcastsos.network

import com.example.broadcastsos.interfaces.IAuthTwitter

import android.util.Log
import com.example.broadcastsos.interfaces.INetworkAccess
import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.model.Response
import com.github.scribejava.core.oauth.OAuth10aService
import kotlinx.coroutines.*

class Handler(private val view: IAuthTwitter) : INetworkAccess {
    private var coroutineScope: CoroutineScope? = null

    private val errorHandler = CoroutineExceptionHandler { context, error ->
        logOut("Async Exception")
        coroutineScope?.launch(Dispatchers.Main) {
            logOut("Async Exception Result")
            print(error.message)
            view.updateAuthUrl(error.localizedMessage ?: "")
        }
    }


    override fun verifyToken(oauthToken: String, oauthTokenSecret: String, oauthVerifier: String) {
        coroutineScope?.cancel()
        coroutineScope = MainScope()
        coroutineScope?.launch(errorHandler) {
            try {
                val defer = async(Dispatchers.IO) {
                    val service: OAuth10aService = ServiceBuilder("Aybi4VfPWujGV6RBAope2Y23k")
                        .apiSecret("dT8dVSAImrX2ByaTLKG5tgkCRqoNHRlkHRNhxG5Z2MfztdGd5j")
                        .build(TwitterApi.instance())
                    Log.i("Handler", "verifyToken")
                    Log.i("oauthToken", oauthToken)
                    Log.i("oauthTokenSecret", oauthTokenSecret)
                    Log.i("oauthVerifier", oauthVerifier)
                    val oauthToken = OAuth1RequestToken(oauthToken, oauthTokenSecret);
                    val accessToken: OAuth1AccessToken = service.getAccessToken(oauthToken, oauthVerifier);
                    Log.i("accessToken", accessToken.rawResponse)
                    Network.Result.NetworkResult2(accessToken.token , accessToken.tokenSecret ).apply {
                        logOut("Async Fetch Done")
                    }
                }
                when (val result = defer.await()) {
                    is Network.Result.NetworkResult2 -> {
                        view.saveAccessToken(result.accessToken, result.accessTokenSecret)
                        logOut("Async Post Success Result")
                    }
                }
            } catch (e: CancellationException) {
                logOut("Async Cancel Result")
            }
        }
    }

    override fun fetchData() {
        coroutineScope?.cancel()
        coroutineScope = MainScope()
        coroutineScope?.launch(errorHandler) {
            try {
                val defer = async(Dispatchers.IO) {
                    logOut("Async Fetch Started")
                    var requestToken: OAuth1RequestToken ?= null
                    val service: OAuth10aService = ServiceBuilder("Aybi4VfPWujGV6RBAope2Y23k")
                        .apiSecret("dT8dVSAImrX2ByaTLKG5tgkCRqoNHRlkHRNhxG5Z2MfztdGd5j")
                        .build(TwitterApi.instance())

                    requestToken = service.requestToken;
                    Log.i("requestToken", requestToken.token)
                    Log.i("requestToken", requestToken.tokenSecret)
                    val authURL = service.getAuthorizationUrl(requestToken)
                    Log.i("authURL", authURL)
                    Network.Result.NetworkResult(authURL, requestToken.token, requestToken.tokenSecret).apply {
                        logOut("Async Fetch Done")
                    }

                }
                when (val result = defer.await()) {
                    is Network.Result.NetworkResult -> {
                        view.updateAuthUrl(result.authUrl)
                        view.saveOauthToken(result.requestToken, result.requestTokenSecret)
                        logOut("Async Post Success Result")
                    }
                }
            } catch (e: CancellationException) {
                logOut("Async Cancel Result")
            }
        }
    }

    override fun terminate() {
        coroutineScope?.cancel()
    }
}

