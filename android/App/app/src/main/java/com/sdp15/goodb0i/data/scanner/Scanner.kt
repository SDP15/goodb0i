package com.sdp15.goodb0i.data.scanner

import android.graphics.Bitmap

interface Scanner {

    fun scanImage(ba: ByteArray, rotation: Int, width: Int, height: Int, callback: (BarcodeReading) -> Unit)

}