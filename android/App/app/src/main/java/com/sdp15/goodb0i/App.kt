package com.sdp15.goodb0i

import android.app.Application
import com.google.firebase.FirebaseApp
import com.sdp15.goodb0i.data.scanner.BarcodeReader
import com.sdp15.goodb0i.data.scanner.MLKitScanner
import com.sdp15.goodb0i.data.sockets.SocketHandler
import com.sdp15.goodb0i.data.store.items.RetrofitItemLoader
import com.sdp15.goodb0i.data.store.items.TestDataItemLoader
import com.sdp15.goodb0i.data.store.lists.ListManager
import com.sdp15.goodb0i.data.store.lists.RetrofitListManager
import com.sdp15.goodb0i.view.confirmation.ConfirmationViewModel
import com.sdp15.goodb0i.view.connection.devices.DeviceListViewModel
import com.sdp15.goodb0i.view.item.ItemViewModel
import com.sdp15.goodb0i.view.list.ListViewModel
import com.sdp15.goodb0i.view.orders.OrdersViewModel
import com.sdp15.goodb0i.view.pin.PinViewModel
import com.sdp15.goodb0i.view.scanner.ScannerViewModel
import com.sdp15.goodb0i.view.welcome.WelcomeViewModel
import org.koin.android.ext.android.startKoin
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.dsl.module.module
import org.koin.log.Logger
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        FirebaseApp.initializeApp(this) // Initialise FireBase for MLKit

        // Start up dependency injection
        startKoin(this, modules, logger = object : Logger {
            override fun debug(msg: String) = Timber.d(msg)

            override fun err(msg: String) = Timber.e(msg)

            override fun info(msg: String) = Timber.i(msg)
        })
    }

    var shouldUseTestData = false

    private val modules = listOf(
        module {
            viewModel<PinViewModel>()
            viewModel<ScannerViewModel>()
            viewModel<WelcomeViewModel>()
            viewModel<ConfirmationViewModel>()
            viewModel<ItemViewModel>()
            viewModel<OrdersViewModel>()
            viewModel<ListViewModel>()
            viewModel<DeviceListViewModel>()
        },
        module {
            factory { if (shouldUseTestData) TestDataItemLoader else RetrofitItemLoader }
            single<ListManager> { RetrofitListManager }
            single<BarcodeReader> { MLKitScanner() }
            single<SocketHandler> { SocketHandler() }
        }
    )
}