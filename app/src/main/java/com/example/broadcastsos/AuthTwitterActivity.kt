
package com.example.broadcastsos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.broadcastsos.interfaces.IAuthTwitter
import com.example.broadcastsos.databinding.ActivityAuthTwitterBinding
import com.example.broadcastsos.network.Handler
import com.example.broadcastsos.services.ShakeService

class AuthTwitterActivity : AppCompatActivity(), IAuthTwitter {
    private lateinit var binding : ActivityAuthTwitterBinding
    private val networkHandler = Handler(this)
    private lateinit var sharedPref: SharedPreferences;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getOauthToken()

        binding = ActivityAuthTwitterBinding.inflate(layoutInflater)
        sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        setContentView(binding.root)

        binding.verifyButton.setOnClickListener {
            networkHandler.verifyToken(sharedPref.getString("oauthToken", "").toString(), sharedPref.getString("oauthTokenSecret", "").toString(), binding.otpEditText.text.toString())
        }

        binding.showPrefsButton.setOnClickListener {
            sharedPref.all.forEach {
                Log.d("AuthTwitterActivity", "${it.key} : ${it.value}")
            }

        }

        val intent = Intent(this, ShakeService::class.java)
        //Start Service
        startService(intent)
    }

    private fun getOauthToken() {
        networkHandler.fetchData()
    }

    override fun updateAuthUrl(authUrl: String) {
        binding.loginButton.setOnClickListener() {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)))
        }
    }

    override fun saveOauthToken(oauthToken: String, oauthTokenSecret: String) {
        with (sharedPref.edit()) {
            putString("oauthToken", oauthToken)
            putString("oauthTokenSecret", oauthTokenSecret)
            apply()
        }
    }

    override fun saveAccessToken(accessToken: String, accessTokenSecret: String) {
        with (sharedPref.edit()) {
            putString("accessToken", accessToken)
            putString("accessTokenSecret", accessTokenSecret)
            apply()
        }
    }
}