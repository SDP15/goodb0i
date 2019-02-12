package com.sdp15.goodb0i.data.store.lists

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.sdp15.goodb0i.data.store.APIError
import com.sdp15.goodb0i.data.store.Result
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import timber.log.Timber

object RetrofitListManager : ListManager {

    private val retrofit = Retrofit.Builder().apply {
        client(OkHttpClient().newBuilder().build())
        baseUrl("http://10.0.2.2:8080") // Machine localhost
        addConverterFactory(GsonConverterFactory.create())
        addCallAdapterFactory(CoroutineCallAdapterFactory())
    }.build()

    private val api = retrofit.create(KTORListAPI::class.java)

    override suspend fun loadList(code: Long): Result<ShoppingList> {
        api.loadListAsync(code.toString()).await().apply {
            val body = body()
            return if (isSuccessful && body != null) {
                Timber.i("Received shopping list $body")
                Result.Success(body)
            } else {
                Timber.i("Error: ${this.errorBody()?.string()}\n${this.message()}\n${this.code()}\n${this.body()}")
                Result.Failure(APIError(this))
            }
        }
    }

    override suspend fun createList(contents: List<Pair<String, Int>>): Result<String> {
        Timber.i("Attempting creation with $contents")
        api.createListAsync(contents).await().apply {
            val body = body()
            return if (isSuccessful && body != null) {
                Timber.i("Created list with code $body")
                Result.Success(body)
            } else {
                Result.Failure(APIError(this))
            }
        }
    }
}

interface KTORListAPI {

    @POST("/lists/new/")
    fun createListAsync(@Body body: List<Pair<String, Int>>): Deferred<Response<String>>

    @GET("/lists/load/{code}")
    fun loadListAsync(@Path("code") code: String): Deferred<Response<ShoppingList>>

}