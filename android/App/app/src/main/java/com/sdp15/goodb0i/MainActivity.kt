package com.sdp15.goodb0i

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.sdp15.goodb0i.data.bluetooth.SafeHandler
import com.sdp15.goodb0i.view.BaseFragment
import com.sdp15.goodb0i.view.connection.BluetoothService
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var bluetoothService: BluetoothService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
    }

    override fun onBackPressed() {
        val fragment = nav_host_fragment.childFragmentManager.fragments[0] // currently visible fragment
        // Pass the back press to the fragment for overriding
        val handled = (fragment as? BaseFragment)?.onBackPressed() ?: false
        if (!handled) {
            super.onBackPressed()
        }
    }

    private var isBound = false //TODO: Will we need to use this for anything

    fun startBluetoothService() {
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun startSearch() {
        bluetoothService?.scan()
    }

    fun addMessageHandler(handler: SafeHandler.MessageHandler) {
        messageHandler.addHandler(handler)
    }

    fun removeMessageHandler(handler: SafeHandler.MessageHandler) {
        messageHandler.removeHandler(handler)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.nav_host_fragment).navigateUp()

    private val messageHandler = SafeHandler.MergedMessageHandler()

    private val connection = object: ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            Timber.i("Service disconnected")
            isBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, service: IBinder) {
            Timber.i("Service connected")
            bluetoothService = (service as BluetoothService.LocalBinder).service
            bluetoothService?.handler = SafeHandler(messageHandler)
            bluetoothService?.scan()
            isBound = true
        }
    }

}
