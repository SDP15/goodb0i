package com.sdp15.goodb0i.data.bluetooth

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceInfo(val name: String, val mac: String, val strength: Short) : Parcelable