package com.sdp15.goodb0i.data.navigation.scanner

import android.hardware.camera2.CameraAccessException
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import timber.log.Timber

class MLKitScanner : BarcodeReader {

    private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(
            FirebaseVisionBarcode.FORMAT_ALL_FORMATS
        )
        .build()

    private val detector = FirebaseVision.getInstance()
        .getVisionBarcodeDetector(options)


    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(rotation: Int) = when (rotation) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> {
            FirebaseVisionImageMetadata.ROTATION_0
        }
    }

    override fun scanImage(ba: ByteArray, rotation: Int, width: Int, height: Int, callback: BarcodeReaderCallback) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(width)
            .setHeight(height)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(getRotationCompensation(rotation))
            .build()
        detector.detectInImage(FirebaseVisionImage.fromByteArray(ba, metadata))
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    Timber.i("Barcodes $barcodes")
                    callback.onBarcodeRead(
                        BarcodeReading(
                            barcodes.first().displayValue,
                            barcodes.first().boundingBox
                        )
                    )
                } else {
                    callback.onNoBarcodesFound()
                }

            }
            .addOnFailureListener {
                Timber.e(it, "Barcode detection failure")
                callback.onNoBarcodesFound()
            }
    }

}