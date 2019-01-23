package com.sdp15.goodboi

import android.app.Application
import com.sdp15.goodboi.view.login.LoginViewModel
import org.koin.android.ext.android.startKoin
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.koin.log.Logger
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        startKoin(this, modules, logger=object: Logger {
            override fun debug(msg: String) = Timber.d(msg)

            override fun err(msg: String) = Timber.e(msg)

            override fun info(msg: String) = Timber.i(msg)
        })

    }

    private val modules = listOf(
        module {
            viewModel<LoginViewModel>()
        }
    )
}