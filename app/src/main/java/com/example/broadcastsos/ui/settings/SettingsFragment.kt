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
import androidx.preference.*
import com.example.broadcastsos.Constants.Companion.GET_FOLLOWERS
import com.example.broadcastsos.R
import com.example.broadcastsos.databinding.FragmentSettingsBinding
import com.example.broadcastsos.services.twitter.rest.models.GetFollowersResponseModel
import com.example.broadcastsos.services.twitter.rest.TwitterViewModel
import com.example.broadcastsos.services.twitter.rest.TwitterService
import com.google.gson.Gson


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
    class SettingsFragment : PreferenceFragmentCompat(), TwitterViewModel {
        private val twitterService: TwitterService by lazy { TwitterService(this) }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
//            networkAccessCoroutinesAsyncAwait.fetchData(Network.httpUrlBuilder, "Boy")
            twitterService.getFollowers(this.requireContext(), GET_FOLLOWERS)

        }

        override fun syncResponse(responseBody: String, responseCode: Int, requestCode: String) {
            if (requestCode == GET_FOLLOWERS) {
                if (responseCode == 200) {
                    val result =
                        Gson().fromJson(responseBody, GetFollowersResponseModel::class.java)
                    val screen = preferenceManager.preferenceScreen as PreferenceScreen
                    val category = PreferenceCategory(context)
                    category.title = "Close Contacts"

                    screen.addPreference(category)

                    val contactList = MultiSelectListPreference(context)
                    contactList.title = "Your close contacts"
                    contactList.summary = "DMs will be sent to these contacts"
                    contactList.entries = result.data.map { it.name }.toTypedArray()
                    contactList.entryValues = result.data.map { it.id }.toTypedArray()
                    contactList.key = "settings_close_contacts"

                    category.addPreference(contactList)
                    preferenceScreen = screen
                } else {
                    Log.i("fetchResponse", "error")
                }
            }
        }
    }


}