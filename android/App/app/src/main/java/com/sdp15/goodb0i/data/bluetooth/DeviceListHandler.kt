package com.sdp15.goodb0i.data.bluetooth

import android.os.Bundle
import android.os.Message
import com.sdp15.goodb0i.view.connection.BluetoothService

//TODO: DO we need more than one listener?
class DeviceListHandler(private val listener: DeviceConnectionListener) : SafeHandler.MessageHandler {

    override fun handleMessage(msg: Message) {
        when(msg.what) {
            BluetoothService.MessageCodes.SCANNED -> {
                listener.scanned()
            }
            BluetoothService.MessageCodes.STATE_CHANGE -> {
                listener.stateChange()
            }
            BluetoothService.MessageCodes.NEW_DEVICE -> {
                val deviceInfo = msg.data.getParcelable<DeviceInfo>(BluetoothService.MessageKeys.DEVICE_INFO)
                deviceInfo?.let { listener.deviceAdded(it) }
            }
        }
    }

    interface DeviceConnectionListener {

        fun deviceAdded(info: DeviceInfo)

        fun stateChange()

        fun scanned()

    }

}
