package com.sdp15.goodb0i.view.navigation.scanner

import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReader
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReaderCallback
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReading
import com.sdp15.goodb0i.view.BaseViewModel
import org.koin.standalone.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class ScannerViewModel : BaseViewModel<Any>(),
    ScannerFragment.ScannerFragmentInteractor {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()

    private val reader: BarcodeReader by inject()
    private val isRunning = AtomicBoolean(false)

    private var lastReading: BarcodeReading? = null
    val reading = MutableLiveData<BarcodeReading>()

    override fun bind() {
    }

    override fun onImageCaptured(ba: ByteArray, rotation: Int, width: Int, height: Int) {
        if (!isRunning.get()) { // If the reader is not already running
            isRunning.set(true)
            Timber.i("Starting new scan")
            reader.scanImage(ba, rotation, width, height, object :
                BarcodeReaderCallback {
                override fun onBarcodeRead(reading: BarcodeReading) {
                    Timber.i("Barcode read $reading")
                    isRunning.set(false)
                    this@ScannerViewModel.reading.postValue(reading)
                }

                override fun onNoBarcodesFound() = isRunning.set(false)

            })
        }
        //Timber.i("Image received")
    }

}