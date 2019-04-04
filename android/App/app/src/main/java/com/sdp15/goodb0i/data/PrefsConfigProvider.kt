package com.sdp15.goodb0i.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.sdp15.goodb0i.R

class PrefsConfigProvider(context: Context) : ConfigProvider {

    private var useTestDataInternal: Boolean = false
    private var serverAddressInternal: String = "http://10.0.0.2:8080"
    private var shouldSkipScannerInternal: Boolean = false

    init {
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            val keyUseTestData = context.getString(R.string.config_key_use_test_data)
            val keyServerAddress = context.getString(R.string.config_key_server_address)
            val keySkipScanner = context.getString(R.string.config_key_skip_scanner)
            registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
                when (key) {
                    keyUseTestData -> {
                        useTestDataInternal = sharedPreferences.getBoolean(key, useTestDataInternal)
                    }
                    keyServerAddress -> {
                        serverAddressInternal = sharedPreferences.getString(key, serverAddressInternal)
                    }
                    keySkipScanner -> {
                        shouldSkipScannerInternal = sharedPreferences.getBoolean(key, shouldSkipScannerInternal)

                    }
                }
            }

        }
    }

    override val useTestData: Boolean
        get() = useTestDataInternal
    override val serverAddress: String
        get() = serverAddressInternal
    override val shouldSkipScanner: Boolean
        get() = shouldSkipScannerInternal
}