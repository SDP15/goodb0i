package com.sdp15.goodb0i.view.scanner

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.sdp15.goodb0i.BaseViewModel
import timber.log.Timber

class ScannerViewModel : BaseViewModel<ScannerViewModel.ScannerAction>(), ScannerFragment.ScannerFragmentInteractor {



    override fun bind() {

    }

    override fun onImageCaptured(image: FirebaseVisionImage) {
        Timber.i("Image received")
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats( // TODO: Find out which code types we need to use
                //TODO: Make configurable in settings
                FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
            .build()

        val detector = FirebaseVision.getInstance()
            .getVisionBarcodeDetector(options)
        val result = detector.detectInImage(image)
            .addOnSuccessListener { barcodes ->
                Timber.i("Barcodes detected $barcodes")
                actions.postValue(ScannerAction.TakeImage)
            }
            .addOnFailureListener {
                Timber.e(it, "Barcode detection failure")
                actions.postValue(ScannerAction.TakeImage)
            }

    }

    sealed class ScannerAction {
        object TakeImage : ScannerAction()
    }

}