package com.sdp15.goodb0i.data.scanner

import android.graphics.Bitmap
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import timber.log.Timber

class MLKitScanner : Scanner {

    private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats( // TODO: Find out which code types we need to use
            //TODO: Make configurable in settings
            FirebaseVisionBarcode.FORMAT_ALL_FORMATS
        )
        .build()

    private val detector = FirebaseVision.getInstance()
        .getVisionBarcodeDetector(options)


    override fun scanImage(image: Bitmap, callback: (BarcodeReading) -> Unit) {
        detector.detectInImage(FirebaseVisionImage.fromBitmap(image))
            .addOnSuccessListener { barcodes ->
                Timber.i("Barcodes detected $barcodes")
                callback(BarcodeReading(0))
            }
            .addOnFailureListener {
                Timber.e(it, "Barcode detection failure")
            }
    }
}