package com.sdp15.goodb0i.view.connection.devices

import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.bluetooth.DeviceInfo
import com.sdp15.goodb0i.data.bluetooth.DeviceListHandler
import timber.log.Timber

class DeviceListViewModel : BaseViewModel<DeviceListViewModel.ConnectionAction>(), DeviceListHandler.DeviceConnectionListener {

    override fun bind() {

    }



    override fun deviceAdded(info: DeviceInfo) {
        Timber.i("Device added $info")
    }

    override fun stateChange() {
    }

    override fun scanned() {
    }

    sealed class ConnectionAction {}

}