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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.broadcastsos.R
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
    private var isTwitterConnected = MutableLiveData(false)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val activity = requireActivity()
        sharedPref = activity.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        isTwitterConnected.observe(viewLifecycleOwner, object : Observer<Boolean?> {
            override fun onChanged(aBoolean: Boolean?) {
                Log.d("isTwitterConnected","$aBoolean")
                if (isTwitterConnected.value == true) {
                    paintAfterTwitterConnects()
                } else {
                    paintAfterTwitterDisconnects()
                }
            }
        })

        val intent = Intent(activity, ShakeService::class.java)
        activity.startService(intent)

        return root
    }

    fun paintAfterTwitterConnects() {
        val loginButton = binding.loginButton
        val verifyButton = binding.verifyButton
        val otpEditText = binding.otpEditText
        loginButton.visibility = View.VISIBLE
        loginButton.text = "Disconnect Twitter"
        loginButton.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "Disconnecting Twitter...", Toast.LENGTH_SHORT).show()
            sharedPref.edit().clear().apply()
            binding.ivDashboardIcon.setImageResource(R.mipmap.ic_launcher_disconnected_round)
            isTwitterConnected.value = false
            Toast.makeText(context, "Twitter disconnected", Toast.LENGTH_SHORT).show()
        })

        verifyButton.visibility = View.GONE
        otpEditText.text.clear()
        otpEditText.visibility = View.GONE
    }

    fun paintAfterTwitterDisconnects() {
        val loginButton = binding.loginButton
        val verifyButton = binding.verifyButton
        val otpEditText = binding.otpEditText
        loginButton.visibility = View.VISIBLE
        loginButton.text = "Step 1: Connect Twitter"
        getOauthTokenAndSetAuthUrl()


        verifyButton.visibility = View.VISIBLE
        verifyButton.text = "Step 2: Verify OTP"
        verifyButton.setOnClickListener(View.OnClickListener {
            val otp = otpEditText.text.toString()
            if (otp.isEmpty()) {
                Toast.makeText(context, "Please enter OTP", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Verifying OTP...", Toast.LENGTH_SHORT).show()
                networkHandler.verifyToken(sharedPref.getString("oauthToken", "").toString(), sharedPref.getString("oauthTokenSecret", "").toString(), otp)
            }
        })
        otpEditText.visibility = View.VISIBLE

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun getOauthTokenAndSetAuthUrl() {
        networkHandler.fetchRequestTokenAndAuthUrl()
    }

    override fun saveOauthTokenAndUpdateAuthUrl(oauthToken: String, oauthTokenSecret: String, authUrl: String) {
        binding.loginButton.setOnClickListener() {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)))
        }
        with (sharedPref.edit()) {
            putString("oauthToken", oauthToken)
            putString("oauthTokenSecret", oauthTokenSecret)
            apply()
        }
        Log.i("DashboardFragment", "oauthToken: $oauthToken")
        Log.i("DashboardFragment", "oauthTokenSecret: $oauthTokenSecret")
    }

    override fun saveAccessToken(accessToken: String, accessTokenSecret: String) {
        with (sharedPref.edit()) {
            putString("accessToken", accessToken)
            putString("accessTokenSecret", accessTokenSecret)
            apply()
        }
        binding.ivDashboardIcon.setImageResource(R.mipmap.ic_launcher_connected_round)
        isTwitterConnected.value = true
        Toast.makeText(activity, "Connected to Twitter!", Toast.LENGTH_SHORT).show()
    }

    override fun errorOnVerifyingToken() {
        binding.ivDashboardIcon.setImageResource(R.mipmap.ic_launcher_disconnected_round)
        Toast.makeText(activity, "Error verifying OTP", Toast.LENGTH_SHORT).show()
    }

    override fun errorOnSavingOauthTokenOrUpdatingAuthUrl() {
        binding.ivDashboardIcon.setImageResource(R.mipmap.ic_launcher_disconnected_round)
        Toast.makeText(activity, "Error connecting to Twitter", Toast.LENGTH_SHORT).show()
    }
}