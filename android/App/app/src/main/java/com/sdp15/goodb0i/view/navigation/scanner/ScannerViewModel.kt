package com.sdp15.goodb0i.view.navigation.scanner

import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.navigation.Message
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReader
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReaderCallback
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReading
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.view.BaseViewModel
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
        sm.currentProducts.observeForever(object: Observer<List<ListItem>> {
            override fun onChanged(list: List<ListItem>) {
                if(Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")
                        || Build.MANUFACTURER.contains("Genymotion")
                        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                        || "google_sdk".equals(Build.PRODUCT)) {
                    Timber.i("On Emulator: Sending products id ${list.first().product.id} for $list")
                    onRead(list.first().product.id)
                }
                sm.currentProducts.removeObserver(this)
            }
        })
    }

    private fun onRead(code: String) {
        isRunning.set(true)
        launch {
            val product = sm.checkScannedCode(code)
            if (product != null) {
                transitions.postValue(ScannerFragmentDirections.actionScannerFragmentToConfirmationFragment())
            } else {
                isRunning.set(false)
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