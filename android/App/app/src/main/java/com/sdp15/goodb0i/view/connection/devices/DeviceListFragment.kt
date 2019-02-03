package com.sdp15.goodb0i.view.connection.devices

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.MainActivity
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.bluetooth.DeviceListHandler
import com.sdp15.goodb0i.view.BaseFragment
import com.sdp15.goodb0i.view.ListDiff
import kotlinx.android.synthetic.main.layout_bluetooth.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeviceListFragment : BaseFragment() {

    private val vm: DeviceListViewModel by viewModel()
    private val adapter = DeviceAdapter()
    private lateinit var deviceHandler: DeviceListHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_bluetooth, container, false)
    }

    override fun onResume() {
        super.onResume()
        deviceHandler = DeviceListHandler(vm)
        (activity as MainActivity).addMessageHandler(deviceHandler)
        (activity as MainActivity).startSearch()
        bluetooth_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        bluetooth_recycler.adapter = adapter
        vm.bluetoothDevices.observe(this, Observer {
            if (it is ListDiff.Add) {
                adapter.addDevice(it.item)
            }
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (activity as MainActivity).startBluetoothService()

    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity).removeMessageHandler(deviceHandler)
    }
}