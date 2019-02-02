package com.sdp15.goodb0i.view.connection

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import com.sdp15.goodb0i.BuildConfig
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.bluetooth.DeviceInfo
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService : Service() {
    

    object MessageKeys {
        const val DEVICE_INFO = "device_info"
        const val TOAST = "toast"
    }
    
    object MessageCodes {
        const val CONNECTED = 0
        const val STATE_CHANGE = 1
        const val SCANNED = 2
        const val WRITE = 3
        const val DEVICE_NAME = 4
        const val TOAST = 5
        const val NEW_DEVICE = 6
    }
    
    object ConnectionState {
        const val NONE = 0
        const val CONNECTING = 1
        const val CONNECTED = 2
    }
    
    object MessageConstants {
        const val READ = 0
        const val WRITE = 1
    }

    private var state = ConnectionState.NONE

    private val bondedDevices = mutableListOf<BluetoothDevice>()
    private val devicesInfo = mutableListOf<DeviceInfo>()
    private val binder = LocalBinder()
    var handler: Handler? = null
    private var connectThread: ConnectThread? = null
    private var transferThread: TransferThread? = null

    private lateinit var adapter: BluetoothAdapter

    override fun onCreate() {
        super.onCreate()
        // Will crash if bluetoothis not supported (on the emulator)
        adapter = BluetoothAdapter.getDefaultAdapter()
    }

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        receiver.abortBroadcast()
        adapter.disable()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Timber.i("Bluetooth device found $device")

                // Signal strength in dBm
                val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                if (!devicesInfo.any { it.mac == device.address } && device.name.isNotBlank()) {
                    devicesInfo.add(DeviceInfo(device.name, device.address, rssi))

                    handler?.let {
                        val message = it.obtainMessage(MessageCodes.NEW_DEVICE)
                        val bundle = Bundle()
                        bundle.putParcelable(MessageKeys.DEVICE_INFO, device)
                        message.data = bundle
                        it.sendMessage(message)
                    }
                }
            }
        }
    }

    fun scan() {
        Timber.i("Beginning scan")
        devicesInfo.clear()
        bondedDevices.clear()
        bondedDevices.addAll(adapter.bondedDevices)

        devicesInfo.addAll(bondedDevices.map { DeviceInfo(it.name, it.address, 100) })
        Timber.i("Before scan $devicesInfo")
        applicationContext.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        adapter.startDiscovery()
    }

    private fun connect(index: Int) {
        connectThread?.cancel()
        transferThread?.cancel()
        transferThread = null
        connectThread = ConnectThread(bondedDevices[index])
        connectThread?.start()
    }

    private fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
        connectThread?.cancel()
        connectThread = null

        transferThread?.cancel()
        transferThread = TransferThread(socket)
        transferThread?.start()
    }

    private fun connectionFailed() {
        handler?.let {
            val message = it.obtainMessage(MessageCodes.TOAST)
            val bundle = Bundle()
            bundle.putString(MessageKeys.TOAST, getString(R.string.error_bluetooth_unable_to_connect))
            message.data = bundle
            it.sendMessage(message)
        }
        state = ConnectionState.NONE
    }

    private fun connectionLost() {
        handler?.let {
            val message = it.obtainMessage(MessageCodes.TOAST)
            val bundle = Bundle()
            bundle.putString(MessageKeys.TOAST, getString(R.string.error_bluetooth_connection_lost))
            message.data = bundle
            it.sendMessage(message)
        }
        state = ConnectionState.NONE
    }

    public fun write(command: String) {
        synchronized(this) {
            if (state != ConnectionState.CONNECTED) return
            transferThread?.write("[$command]".toByteArray())
        }
    }

    public fun write(command: String, arg: String) {
        synchronized(this) {
            if (state != ConnectionState.CONNECTED) return
            transferThread?.write("[$command/$arg]".toByteArray())
        }
    }

    inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(BuildConfig.APPLICATION_ID))
            } catch (e: IOException) {
                Timber.e(e, "Socket creation failed")
            }
        }

        override fun run() {
            super.run()
            adapter.cancelDiscovery() // apparently discovery may slow down the detection
            socket?.let {
                try {
                    it.connect()
                    synchronized(this@BluetoothService) {
                        connectThread = null
                    }
                    Timber.i("Connected to $socket")
                    connected(it, device)
                } catch (e: IOException) {
                    Timber.e(e, "Couldn't connect socket")
                    try {
                        it.close()
                    } catch (e: IOException) {
                        Timber.e(e, "Couldn't close socket")
                    }
                    connectionFailed()
                }
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Timber.e(e, "Couldn't cancel socket")
            }
        }

    }
    inner class TransferThread(private val socket: BluetoothSocket) : Thread() {
        private var inStream: InputStream = socket.inputStream
        private var outStream: OutputStream? = socket.outputStream
        private var buffer: ByteArray = byteArrayOf()

        init {
            this@BluetoothService.state = ConnectionState.CONNECTED
            handler?.obtainMessage(MessageCodes.STATE_CHANGE, this@BluetoothService.state, -1)?.sendToTarget()

        }

        override fun run() {
            super.run()
            buffer = ByteArray(1024)

            while (this@BluetoothService.state == ConnectionState.CONNECTED) {
                try {
                    val numBytes = inStream.read(buffer)
                    handler?.let {
                        val message = it.obtainMessage(MessageConstants.READ, numBytes, -1, buffer)
                        message.sendToTarget()
                    }
                } catch (e: IOException) {
                    Timber.e(e, "Input stream disconnected")
                    connectionLost()
                    break
                }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outStream?.write(bytes)

                handler?.let {
                    val message = it.obtainMessage(MessageConstants.WRITE, -1, -1, buffer)
                    message.sendToTarget()
                }
            } catch (e: IOException) {
                Timber.e(e, "Exception occurred sending data")
            }
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Timber.e("Could not close socket")
            }
        }

    }

    inner class LocalBinder : Binder() {
        val service = this@BluetoothService
    }

}