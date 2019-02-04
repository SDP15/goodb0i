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


    override fun bind() {

    }


    override fun onImageCaptured(ba: ByteArray, rotation: Int, width: Int, height: Int) {
        if (!isRunning.get()) {
            isRunning.set(true)
            Timber.i("Starting new scan")
            scanner.scanImage(ba, rotation, width, height) {
                if (it.fvb.isNotEmpty()) {
                    Timber.i("Barcodes ${it.fvb.first()}")
                }
                isRunning.set(false)
            }
        }
        //Timber.i("Image received")
    }

    sealed class ScannerAction {
        object TakeImage : ScannerAction()
    }

}