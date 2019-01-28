package com.sdp15.goodb0i.data.scanner

import android.graphics.Bitmap

interface Scanner {

    fun scanImage(image: Bitmap, callback: (BarcodeReading) -> Unit)

}