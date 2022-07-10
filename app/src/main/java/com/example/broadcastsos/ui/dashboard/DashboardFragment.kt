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

    private val constext = getContext()

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
                    paintWhenTwitterConnects()
                } else {
                    paintWhenTwitterDisconnects()
                }
            }
        })

        val intent = Intent(activity, ShakeService::class.java)
        activity.startService(intent)

        return root
    }

    fun paintWhenTwitterConnects() {
        val loginButton = binding.loginButton
        val verifyButton = binding.verifyButton
        val otpEditText = binding.otpEditText
        val ivDashboardIcon = binding.ivDashboardIcon
        loginButton.visibility = View.VISIBLE
        loginButton.text = "Disconnect Twitter"
        loginButton.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "Disconnecting Twitter...", Toast.LENGTH_SHORT).show()
            sharedPref.edit().clear().apply()
            Toast.makeText(context, "Twitter disconnected", Toast.LENGTH_SHORT).show()
            isTwitterConnected.value = false
        })

        verifyButton.visibility = View.GONE
        otpEditText.text.clear()
        otpEditText.visibility = View.GONE
        ivDashboardIcon.setImageResource(R.mipmap.ic_launcher_connected_round)
    }

    fun paintWhenTwitterDisconnects() {
        val loginButton = binding.loginButton
        val verifyButton = binding.verifyButton
        val otpEditText = binding.otpEditText
        loginButton.visibility = View.VISIBLE
        loginButton.text = "Step 1: Connect Twitter"
        getOauthToken()


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
}