package com.example.broadcastsos.services.twitter.rest

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.broadcastsos.Keys
import kotlinx.coroutines.*
import okhttp3.*
import okio.Buffer
import okio.ByteString
import java.io.IOException
import java.net.URLEncoder
import java.security.GeneralSecurityException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class TwitterService(private val viewModel: TwitterViewModel) : ITwitterApis {
    private lateinit var sharedPref: SharedPreferences;
    private var coroutineScope: CoroutineScope? = null

    private val errorHandler = CoroutineExceptionHandler { context, error ->
        coroutineScope?.launch(Dispatchers.Main) {
            error.printStackTrace()
        }
    }
    private fun getOauthKeys(context: Context) =
        OauthKeys(
            consumerKey = Keys.CONSUMER_KEY,
            consumerSecret = Keys.CONSUMER_SECRET,
            accessToken = sharedPref.getString("accessToken", ""),
            accessSecret = sharedPref.getString("accessTokenSecret", "")
        )

    override fun sendTweet(context: Context, tweet: String) {

        coroutineScope?.cancel()
        coroutineScope = MainScope()
        coroutineScope?.launch(errorHandler) {
            try {
                val defer = async(Dispatchers.IO) {
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
                    }
                }
                when (val result = defer.await()) {
                    is Response -> {
                    }
                }
            } catch (e: CancellationException) {
            }
        }

    }

    override fun getFollowers(context: Context, requestCode: String) {
        coroutineScope?.cancel()
        coroutineScope = MainScope()
        coroutineScope?.launch(errorHandler) {
            try {
                val defer = async(Dispatchers.IO) {
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
                    }
                }
                when (val result = defer.await()) {
                    is Response -> {
                        val body = result.body()?.string() ?: ""
                        viewModel.syncResponse(body, result.code(),  requestCode)
                    }
                }
            } catch (e: CancellationException) {
            }
        }
    }

    override fun sendDM(context: Context, recipientId: String, msg: String, requestCode: String) {
        coroutineScope?.cancel()
        coroutineScope = MainScope()
        coroutineScope?.launch(errorHandler) {
            try {
                val defer = async(Dispatchers.IO) {
                    sharedPref = context.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
                    val oauthKeys = getOauthKeys(context)
                    val oauth1 = Oauth1SigningInterceptor(oauthKeys = oauthKeys)
                    val userId = sharedPref.getString("userId", "") ?: ""
                    Log.i("userId", userId)
                    val mediaTypeJson = MediaType.parse("application/json; charset=utf-8")

                    val body: RequestBody = RequestBody.create(mediaTypeJson, "{\"event\": {\"type\": \"message_create\", \"message_create\": {\"target\": {\"recipient_id\": \"${recipientId}\"}, \"message_data\": {\"text\": \"${msg}\"}}}}")
                    val request = Request.Builder()
                        .url("https://api.twitter.com/1.1/direct_messages/events/new.json")
                        .post(body)
                        .build()

                    val signed = oauth1.signRequest(request)

                    Network.fetchHttpResult(signed).apply {
                    }
                }
                when (val result = defer.await()) {
                    is Response -> {
                        val body = result.body()?.string() ?: ""
                        Log.i("body", body)
                        viewModel.syncResponse(body, result.code(),  requestCode)
                    }
                }
            } catch (e: CancellationException) {
            }
        }
    }
}



class Oauth1SigningInterceptor(val oauthKeys: OauthKeys,
                               val nonce: String = UUID.randomUUID().toString(),
                               val timestamp: Long = System.currentTimeMillis() / 1000L) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(signRequest(chain.request()))
    }

    @Throws(IOException::class)
    fun signRequest(request: Request): Request {

        //Setup default parameters that will be sent with authorization header
        val parameters = hashMapOf(
            OAUTH_CONSUMER_KEY to oauthKeys.consumerKey,
            OAUTH_NONCE to nonce,
            OAUTH_SIGNATURE_METHOD to OAUTH_SIGNATURE_METHOD_VALUE,
            OAUTH_TIMESTAMP to timestamp.toString(),
            OAUTH_VERSION to OAUTH_VERSION_VALUE
        )
        oauthKeys.accessToken?.let { parameters[OAUTH_TOKEN] = it }

        //Copy query parameters into param map
        val url = request.url()
        for (i in 0 until url.querySize()) {
            parameters[url.queryParameterName(i)] = url.queryParameterValue(i)
        }

        //Create signature
        val method = request.method().encodeUtf8()
        val baseUrl = request.url().newBuilder().query(null).build().toString().encodeUtf8()
        val signingKey = "${oauthKeys.consumerSecret.encodeUtf8()}&${oauthKeys.accessSecret?.encodeUtf8()
            ?: ""}"
        val params = parameters.encodeForSignature()
        val dataToSign = "$method&$baseUrl&$params"
        parameters[OAUTH_SIGNATURE] = sign(signingKey, dataToSign).encodeUtf8()

        //Create auth header
        val authHeader = "OAuth ${parameters.toHeaderFormat()}"
        return request.newBuilder().addHeader("Authorization", authHeader).build()
    }

    private fun RequestBody.asString() = Buffer().run {
        writeTo(this)
        readUtf8().replace("+", "%2B")
    }

    @Throws(GeneralSecurityException::class)
    private fun sign(key: String, data: String): String {
        val secretKey = SecretKeySpec(key.toBytesUtf8(), "HmacSHA1")
        val macResult = Mac.getInstance("HmacSHA1").run {
            init(secretKey)
            doFinal(data.toBytesUtf8())
        }
        return ByteString.of(*macResult).base64()
    }

    private fun String.toBytesUtf8() = this.toByteArray()

    private fun HashMap<String, String>.toHeaderFormat() =
        filter { it.key in baseKeys }
            .toList()
            .sortedBy { (key, _) -> key }
            .toMap()
            .map { "${it.key}=\"${it.value}\"" }
            .joinToString(", ")


    private fun HashMap<String, String>.encodeForSignature() =
        toList()
            .sortedBy { (key, _) -> key }
            .toMap()
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .encodeUtf8()

    private fun String.encodeUtf8() = URLEncoder.encode(this, "UTF-8").replace("+", "%2B")

    companion object {
        private const val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
        private const val OAUTH_NONCE = "oauth_nonce"
        private const val OAUTH_SIGNATURE = "oauth_signature"
        private const val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
        private const val OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1"
        private const val OAUTH_TIMESTAMP = "oauth_timestamp"
        private const val OAUTH_TOKEN = "oauth_token"
        private const val OAUTH_VERSION = "oauth_version"
        private const val OAUTH_VERSION_VALUE = "1.0"

        private val baseKeys = arrayListOf(
            OAUTH_CONSUMER_KEY,
            OAUTH_NONCE,
            OAUTH_SIGNATURE,
            OAUTH_SIGNATURE_METHOD,
            OAUTH_TIMESTAMP,
            OAUTH_TOKEN,
            OAUTH_VERSION
        )
    }
}

data class OauthKeys(
    val consumerKey: String,
    val consumerSecret: String,
    val accessToken: String? = null,
    val accessSecret: String? = null
)
