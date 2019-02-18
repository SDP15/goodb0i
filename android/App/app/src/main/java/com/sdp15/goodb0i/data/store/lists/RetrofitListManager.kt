package com.sdp15.goodb0i.data.store.lists

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.sdp15.goodb0i.data.store.awaitCatching
import com.sdp15.goodb0i.data.store.Result
import com.sdp15.goodb0i.data.store.toResult
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object RetrofitListManager : ListManager {

    private val retrofit = Retrofit.Builder().apply {
        client(OkHttpClient().newBuilder().build())
        baseUrl("http://10.0.2.2:8080") // Machine localhost
        addConverterFactory(GsonConverterFactory.create())
        addCallAdapterFactory(CoroutineCallAdapterFactory())
    }.build()

    private val api = retrofit.create(KTORListAPI::class.java)

    override suspend fun loadList(code: Long): Result<ShoppingList> = api.loadListAsync(code.toString()).awaitCatching(
        success =  { it.toResult() },
        failure = { Result.Failure(Exception(it.message))}
    )

    override suspend fun createList(contents: List<Pair<String, Int>>): Result<String> = api.createListAsync(contents).awaitCatching(
        success = { it.toResult() },
        failure = { Result.Failure(Exception(it.message))}
    )

    override suspend fun updateList(code: Long, contents: List<Pair<String, Int>>): Result<String> = api.updateListAsync(code.toString(), contents).awaitCatching(
        success = {it.toResult() },
        failure = { Result.Failure(Exception(it.message))}
    )
}

interface KTORListAPI {

    @POST("/lists/new/")
    fun createListAsync(@Body body: List<Pair<String, Int>>): Deferred<Response<String>>

    @POST("/lists/update/{code}")
    fun updateListAsync(@Path("code") code: String, @Body body: List<Pair<String, Int>>): Deferred<Response<String>>

    @GET("/lists/load/{code}")
    fun loadListAsync(@Path("code") code: String): Deferred<Response<ShoppingList>>

}