package com.example.broadcastsos.ui.settings

//import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.example.broadcastsos.R
import com.example.broadcastsos.databinding.FragmentSettingsBinding
import com.example.broadcastsos.services.twitter.rest.MainView
import com.example.broadcastsos.services.twitter.rest.Network
import com.example.broadcastsos.services.twitter.rest.NetworkAccessCoroutinesAsyncAwait


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        if (savedInstanceState == null) {
            val supportFragmentManager = requireActivity().supportFragmentManager;
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)



        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    class SettingsFragment : PreferenceFragmentCompat(), MainView {
        private val networkAccessCoroutinesAsyncAwait = NetworkAccessCoroutinesAsyncAwait(this)

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            networkAccessCoroutinesAsyncAwait.fetchData(Network.httpUrlBuilder, "Boy")
        }

        override fun updateScreen(result: String) {
            Log.i("updateScreen", result)
        }
    }


}