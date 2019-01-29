package com.sdp15.goodb0i

import android.app.Application
import com.google.firebase.FirebaseApp
import com.sdp15.goodb0i.data.store.ItemLoader
import com.sdp15.goodb0i.data.store.TestDataItemLoader
import com.sdp15.goodb0i.view.login.LoginViewModel
import com.sdp15.goodb0i.view.scanner.ScannerViewModel
import org.koin.android.ext.android.startKoin
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.dsl.module.module
import org.koin.log.Logger
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        FirebaseApp.initializeApp(this)

        startKoin(this, modules, logger=object: Logger {
            override fun debug(msg: String) = Timber.d(msg)

            override fun err(msg: String) = Timber.e(msg)

            override fun info(msg: String) = Timber.i(msg)
        })

    }

    private val modules = listOf(
        module {
            viewModel<LoginViewModel>()
            viewModel<ScannerViewModel>()
        },
        module {
            single<ItemLoader> { TestDataItemLoader }
        }
    )
}