package com.sdp15.goodb0i.view.scanner

import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.scanner.BarcodeReading
import com.sdp15.goodb0i.data.scanner.Scanner
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class ScannerViewModel : BaseViewModel<ScannerViewModel.ScannerAction>(), ScannerFragment.ScannerFragmentInteractor,
    KoinComponent {

    private val scanner: Scanner by inject()
    private val isRunning = AtomicBoolean(false)

    private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats( // TODO: Find out which code types we need to use
            //TODO: Make configurable in settings
            FirebaseVisionBarcode.FORMAT_ALL_FORMATS
        )
        .build()

    private val detector = FirebaseVision.getInstance()
        .getVisionBarcodeDetector(options)

    override fun bind() {

    }

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

    override fun onImageCaptured(ba: ByteArray, rotation: Int, width: Int, height: Int) {
        if (!isRunning.get()) {
            isRunning.set(true)
            val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth(width)   // 480x360 is typically sufficient for
                .setHeight(height)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(getRotationCompensation(rotation))
                .build()
            detector.detectInImage(FirebaseVisionImage.fromByteArray(ba, metadata))
                .addOnSuccessListener { barcodes ->
                    if(barcodes.isNotEmpty()) Timber.i("Barcodes detected $barcodes")

                    isRunning.set(false)
                }
                .addOnFailureListener {
                    Timber.e(it, "Barcode detection failure")
                    isRunning.set(false)
                }
        }
        //Timber.i("Image received")
    }

    sealed class ScannerAction {
        object TakeImage : ScannerAction()
    }

}