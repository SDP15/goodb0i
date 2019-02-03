package com.sdp15.goodb0i.view.connection.devices

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sdp15.goodb0i.MainActivity
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.bluetooth.DeviceListHandler
import com.sdp15.goodb0i.view.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeviceListFragment : BaseFragment() {

    private val vm: DeviceListViewModel by viewModel()
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
        //TODO: Check that bluetooth is enabled
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