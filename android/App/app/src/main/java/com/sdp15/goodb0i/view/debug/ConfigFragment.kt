package com.sdp15.goodb0i.view.debug

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.store.RetrofitProvider
import org.koin.android.ext.android.inject
import retrofit2.Retrofit
import timber.log.Timber

class ConfigFragment : PreferenceFragmentCompat() {

    private lateinit var root: EditTextPreference
    private val sessionManager: ShoppingSessionManager by inject()
    private val session: ShoppingSession by sessionManager
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        root = findPreference(getString(R.string.config_key_server_address)) as EditTextPreference
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(changeListener)
        root.onPreferenceChangeListener = object: Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(preference: Preference, value: Any?): Boolean {
                if (value !is String) return false
                return try {
                    Retrofit.Builder().apply {
                        baseUrl(value)
                    }
                    Timber.i("Attempting change to root value $value")
                    RetrofitProvider.root = value
                    true
                } catch (iae: IllegalArgumentException) {
                    Timber.e(iae, "Invalid base url")
                    Toast.makeText(context, R.string.error_invalid_server_address_format, Toast.LENGTH_LONG).show()
                    false
                }
            }
        }
        findPreference(getString(R.string.config_kill_session)).setOnPreferenceClickListener {
            session.endSession()
            sessionManager.closeSession()
            true
        }

    }

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == getString(R.string.config_key_use_test_data)) {

        } else if (key == getString(R.string.config_key_server_address)) {
            RetrofitProvider.root = prefs.getString(key, "") ?: ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(changeListener)
    }
}