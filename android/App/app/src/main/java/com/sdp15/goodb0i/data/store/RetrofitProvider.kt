package com.sdp15.goodb0i.data.store

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException
import kotlin.reflect.KProperty

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
            client(OkHttpClient().newBuilder().addInterceptor(object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    try {
                        return chain.proceed(chain.request())
                    } catch (e: Throwable) {
                        Timber.e(e, "Intercepted")
                        if (e is IOException) {
                            throw e
                        } else {
                            throw IOException(e)
                        }
                    }
                }
            }).build())
            baseUrl(root)
            addConverterFactory(GsonConverterFactory.create())
        }.build()

    private var retrofit = build()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Retrofit {
        return retrofit
    }


}