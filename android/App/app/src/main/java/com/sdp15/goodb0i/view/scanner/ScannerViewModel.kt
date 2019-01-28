package com.sdp15.goodb0i.view.scanner

import android.graphics.Bitmap
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.scanner.Scanner
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class ScannerViewModel : BaseViewModel<ScannerViewModel.ScannerAction>(), ScannerFragment.ScannerFragmentInteractor, KoinComponent {

    private val scanner: Scanner by inject()

    override fun bind() {

    }

    override fun onImageCaptured(image: Bitmap) {
        scanner.scanImage(image) {
            Timber.i("Image scan successful")
        }
        Timber.i("Image received")



    }

    sealed class ScannerAction {
        object TakeImage : ScannerAction()
    }

}