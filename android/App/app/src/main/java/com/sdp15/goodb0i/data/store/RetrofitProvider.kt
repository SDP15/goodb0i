package com.sdp15.goodb0i.data.store

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

/*
 * Dirty static store which allows us to switch out the base URL
 * TODO: Implement something in the build script to switch out the base url depending on build type
 */
object RetrofitProvider {

    var root = "http://10.0.2.2:8080" // Machine localhost
        set(value) {
            field = value
            retrofit = build()
        }

    private fun build() : Retrofit =
        Retrofit.Builder().apply {
            client(OkHttpClient().newBuilder().build())
            baseUrl(root)
            addConverterFactory(GsonConverterFactory.create())
            addCallAdapterFactory(CoroutineCallAdapterFactory())
        }.build()


    private var retrofit = build()

    val rootedRetrofit: Retrofit
        get() = retrofit


}