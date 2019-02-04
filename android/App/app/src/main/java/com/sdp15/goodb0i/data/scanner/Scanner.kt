package com.sdp15.goodb0i.data.scanner


interface Scanner {

    fun scanImage(ba: ByteArray, rotation: Int, width: Int, height: Int, callback: (BarcodeReading) -> Unit)

}