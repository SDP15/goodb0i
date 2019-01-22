package com.sdp15.goodboi

import android.app.Application
import org.koin.android.ext.android.startKoin
import org.koin.log.Logger
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        startKoin(this, listOf(), logger=object: Logger {
            override fun debug(msg: String) = Timber.d(msg)

            override fun err(msg: String) = Timber.e(msg)

            override fun info(msg: String) = Timber.i(msg)
        })

    }
}