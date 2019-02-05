package com.sdp15.goodb0i.view.scanner

import com.sdp15.goodb0i.BaseViewModel
import com.sdp15.goodb0i.data.scanner.BarcodeReader
import com.sdp15.goodb0i.data.scanner.BarcodeReaderCallback
import com.sdp15.goodb0i.data.scanner.BarcodeReading
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class ScannerViewModel : BaseViewModel<ScannerViewModel.ScannerAction>(), ScannerFragment.ScannerFragmentInteractor,
    KoinComponent {

    private val scanner: BarcodeReader by inject()
    private val isRunning = AtomicBoolean(false)


    override fun bind() {

    }


    override fun onImageCaptured(ba: ByteArray, rotation: Int, width: Int, height: Int) {
        if (!isRunning.get()) {
            isRunning.set(true)
            Timber.i("Starting new scan")
            scanner.scanImage(ba, rotation, width, height, object: BarcodeReaderCallback {
                override fun onBarcodeRead(reading: BarcodeReading) {
                    Timber.i("Barcode read $reading")
                    isRunning.set(false)
                }

                override fun onNoBarcodesFound() = isRunning.set(false)

            })
        }
        //Timber.i("Image received")
    }

    sealed class ScannerAction {
    }

}