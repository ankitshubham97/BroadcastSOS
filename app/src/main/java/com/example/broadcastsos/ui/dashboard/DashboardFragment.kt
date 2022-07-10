package com.example.broadcastsos.ui.dashboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.broadcastsos.databinding.FragmentDashboardBinding
import com.example.broadcastsos.interfaces.IAuthTwitter
import com.example.broadcastsos.network.Handler
import com.example.broadcastsos.services.ShakeService

class DashboardFragment : Fragment(), IAuthTwitter {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val networkHandler = Handler(this)
    private lateinit var sharedPref: SharedPreferences;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val activity = requireActivity()
        sharedPref = activity.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        binding.verifyButton.setOnClickListener {
            Toast.makeText(activity, "Verifying...", Toast.LENGTH_SHORT).show()
            networkHandler.verifyToken(sharedPref.getString("oauthToken", "").toString(), sharedPref.getString("oauthTokenSecret", "").toString(), binding.otpEditText.text.toString())
        }

        binding.showPrefsButton.setOnClickListener {
            sharedPref.all.forEach {
                Log.d("AuthTwitterActivity", "${it.key} : ${it.value}")
            }

        }
        getOauthToken()

        val intent = Intent(activity, ShakeService::class.java)
        //Start Service
        activity.startService(intent)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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