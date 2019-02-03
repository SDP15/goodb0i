package com.sdp15.goodb0i.view.connection.devices

import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.bluetooth.DeviceInfo
import com.sdp15.goodb0i.data.bluetooth.DeviceListHandler
import com.sdp15.goodb0i.view.ListDiff
import timber.log.Timber

class DeviceListViewModel : BaseViewModel<DeviceListViewModel.ConnectionAction>(), DeviceListHandler.DeviceConnectionListener {

    private val devices = mutableListOf<DeviceInfo>()
    val bluetoothDevices = MutableLiveData<ListDiff<DeviceInfo>>()

    override fun bind() {

    }



    override fun deviceAdded(info: DeviceInfo) {
        Timber.i("Device added $info")
        devices.add(info)
        bluetoothDevices.postValue(ListDiff.Add(devices, info))
    }

    override fun stateChange() {
    }

    override fun scanned() {
    }

    sealed class ConnectionAction {}

}