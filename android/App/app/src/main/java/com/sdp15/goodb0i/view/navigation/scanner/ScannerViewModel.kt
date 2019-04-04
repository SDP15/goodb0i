package com.sdp15.goodb0i.view.navigation.scanner

import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sdp15.goodb0i.data.ConfigProvider
import com.sdp15.goodb0i.data.navigation.ShoppingSession
import com.sdp15.goodb0i.data.navigation.ShoppingSessionManager
import com.sdp15.goodb0i.data.navigation.ShoppingSessionState
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReader
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReaderCallback
import com.sdp15.goodb0i.data.navigation.scanner.BarcodeReading
import com.sdp15.goodb0i.view.BaseViewModel
import kotlinx.coroutines.launch
import org.koin.standalone.get
import org.koin.standalone.inject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class ScannerViewModel : BaseViewModel<Any>(),
    ScannerFragment.ScannerFragmentInteractor {

    private val sm: ShoppingSession by get<ShoppingSessionManager>()
    private val configProvider: ConfigProvider by inject()

    private val reader: BarcodeReader by inject()
    private val isRunning = AtomicBoolean(false)

    val reading = MutableLiveData<BarcodeReading>()

    override fun bind() {
        // Restart scanning on bind
        isRunning.set(false)
        // Test code for emulator runs.
        // Automatically 'scans' the correct product
        Timber.i("Fingerprint ${Build.FINGERPRINT}, model ${Build.MODEL}, brand ${Build.BRAND}")
        if (configProvider.shouldSkipScanner) {
            skip()
        }
        sm.state.observeForever { state ->
            if (state is ShoppingSessionState.Disconnected) {
                transitions.postValue(ScannerFragmentDirections.actionScannerFragmentToErrorFragment())
            }
        }
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

    fun manualEntry(code: String) {
        onRead(code)
    }

    fun skip() {
        sm.state.observeForever(object : Observer<ShoppingSessionState> {
            override fun onChanged(state: ShoppingSessionState?) {
                if (state is ShoppingSessionState.Scanning) {
                    Timber.i("Skipping product ${state.toScan.first()}")
                    onRead(state.toScan.first().product.gtin)
                }
                sm.state.removeObserver(this)
            }
        })
    }

}