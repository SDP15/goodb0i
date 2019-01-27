package com.sdp15.goodboi.view.scanner

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.sdp15.goodboi.BaseViewModel

class ScannerViewModel : BaseViewModel<ScannerViewModel.ScannerAction>() {

    private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats( // TODO: Find out which code types we need to use
            FirebaseVisionBarcode.FORMAT_QR_CODE,
            FirebaseVisionBarcode.FORMAT_AZTEC)
        .build()

    override fun bind() {

    }

    sealed class ScannerAction {}

}