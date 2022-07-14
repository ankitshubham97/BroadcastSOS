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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.broadcastsos.Constants.Companion.GET_ME
import com.example.broadcastsos.R
import com.example.broadcastsos.databinding.FragmentDashboardBinding
import com.example.broadcastsos.services.background.RestarterBroadcastReceiver
import com.example.broadcastsos.services.twitter.oauth.interfaces.IAuthTwitter
import com.example.broadcastsos.services.twitter.oauth.network.OauthHandler
import com.example.broadcastsos.services.twitter.rest.TwitterService
import com.example.broadcastsos.services.twitter.rest.TwitterViewModel
import com.example.broadcastsos.services.twitter.rest.models.GetMeResponseModel
import com.google.gson.Gson


class DashboardFragment : Fragment(), IAuthTwitter, TwitterViewModel {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val networkHandler = OauthHandler(this)
    private lateinit var sharedPref: SharedPreferences;
    private var isTwitterConnected = MutableLiveData(false)
    private val twitterService: TwitterService by lazy { TwitterService(this) }

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

        isTwitterConnected.value = sharedPref.getString("accessToken", "") != "" && sharedPref.getString("accessTokenSecret", "") != ""
        if (isTwitterConnected.value == true) {
            binding.ivDashboardIcon.setImageResource(R.mipmap.ic_launcher_connected_round)
        } else {
            binding.ivDashboardIcon.setImageResource(R.mipmap.ic_launcher_disconnected_round)
        }

        RestarterBroadcastReceiver.startWorker(requireContext())

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
        twitterService.getMe(requireContext(), GET_ME)
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
        Glide.with(this).clear(binding.ivDashboardProfile)

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

    override fun saveAccessToken(accessToken: String, accessTokenSecret: String, userId: String) {
        with (sharedPref.edit()) {
            putString("accessToken", accessToken)
            putString("accessTokenSecret", accessTokenSecret)
            putString("userId", userId)
            apply()
        }
        binding.ivDashboardIcon.setImageResource(R.mipmap.ic_launcher_connected_round)
        twitterService.getMe(requireContext(), GET_ME)
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

    override fun syncResponse(responseBody: String, responseCode: Int, requestCode: String) {
        if (requestCode == GET_ME) {
            if (responseCode == 200) {
                val result =
                    Gson().fromJson(responseBody, GetMeResponseModel::class.java)
                Glide.with(requireContext())
                    .load(result.data?.profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.ivDashboardProfile);
            }
        }
    }

}