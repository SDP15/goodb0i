package com.sdp15.goodb0i.view.connection.devices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.bluetooth.DeviceInfo
import kotlinx.android.synthetic.main.list_device.view.*

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private val devices = mutableListOf<DeviceInfo>()

    fun addDevice(device: DeviceInfo) {
        devices.add(device)
        notifyItemInserted(0)
    }

    fun clear() {
        devices.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder =
        DeviceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_device, parent, false))

    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.itemView.device_name.text = devices[position].name
        holder.itemView.device_strength.text = devices[position].strength.toString()
    }

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view)

}