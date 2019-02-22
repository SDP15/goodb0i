package com.sdp15.goodb0i.data.navigation.scanner

import android.graphics.Rect

interface BarcodeReader {

    fun scanImage(ba: ByteArray, rotation: Int, width: Int, height: Int, callback: BarcodeReaderCallback)


}

interface BarcodeReaderCallback {


    fun onBarcodeRead(reading: BarcodeReading)

    fun onNoBarcodesFound()

}

data class BarcodeReading(
    val value: String?,
    val boundingBox: Rect? = null
)