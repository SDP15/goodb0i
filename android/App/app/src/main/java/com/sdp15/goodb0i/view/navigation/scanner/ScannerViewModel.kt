package com.sdp15.goodb0i.view.navigation.scanner

import androidx.lifecycle.MutableLiveData
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReader
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReaderCallback
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReading
import com.sdp15.goodb0i.view.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class ScannerViewModel : BaseViewModel<Any>(),
    ScannerFragment.ScannerFragmentInteractor {

    private val sm: ShoppingSessionManager<Message.IncomingMessage> by inject()

    private val reader: BarcodeReader by inject()
    private val isRunning = AtomicBoolean(false)

    val reading = MutableLiveData<BarcodeReading>()

    override fun bind() {
        // Restart scanning on bind
        isRunning.set(false)
    }

    private fun onRead(code: String) {
        isRunning.set(true)
        GlobalScope.launch(Dispatchers.IO) {
            val product = sm.checkScannedCode(code)
            if (product != null) {
                transitions.postValue(ScannerFragmentDirections.actionScannerFragmentToConfirmationFragment())
            }
        }
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
                    if (reading.value != null) onRead(reading.value)
                    this@ScannerViewModel.reading.postValue(reading)
                }

                override fun onNoBarcodesFound() = isRunning.set(false)

            })
        }
    }

}