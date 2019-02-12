package com.sdp15.goodb0i.data.store

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import timber.log.Timber

object RetrofitListLoader : ListLoader {

    private val retrofit = Retrofit.Builder().apply {
        client(OkHttpClient().newBuilder().build())
        baseUrl("http://10.0.2.2:8080") // Machine localhost
        addConverterFactory(GsonConverterFactory.create())
        addCallAdapterFactory(CoroutineCallAdapterFactory())
    }.build()

    private val api = retrofit.create(KTORListAPI::class.java)

    override suspend fun loadList(code: Long): Result<List<ListItem>> {
        TODO("not implemented")
    }

    override suspend fun createList(contents: List<Pair<String, Int>>): Result<String> {
        Timber.i("Attempting creation with $contents")
        api.createList(contents).await().apply {
            val body = body()
            return if (isSuccessful && body != null) {
                Result.Success("")
            } else {
                Result.Failure(Exception())
            }
        }
    }
}

interface KTORListAPI {

    @POST("/lists/new/")
    fun createList(@Body  body: List<Pair<String, Int>>): Deferred<Response<String>>

}