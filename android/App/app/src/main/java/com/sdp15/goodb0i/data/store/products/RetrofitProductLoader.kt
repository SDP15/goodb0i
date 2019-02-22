package com.sdp15.goodb0i.data.store.products

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.sdp15.goodb0i.data.store.Result
import com.sdp15.goodb0i.data.store.awaitCatching
import com.sdp15.goodb0i.data.store.toResult
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

object RetrofitProductLoader : ProductLoader {

    private val retrofit = Retrofit.Builder().apply {
        client(OkHttpClient.Builder().build())
        baseUrl("http://10.0.2.2:8080") // Machine localhost
        addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        addCallAdapterFactory(CoroutineCallAdapterFactory())
    }.build()

    private val api = retrofit.create(KTORProductAPI::class.java)

    override suspend fun loadProduct(id: String): Result<Product> = api.getProductAsync(id).awaitCatching(
        success = { it.toResult() },
        failure = { Result.Failure(Exception(it.message)) }
    )

    override suspend fun loadCategory(category: String): Result<List<Product>> =
        api.searchAsync(category).awaitCatching(
            success = { it.toResult() },
            failure = { Result.Failure(Exception(it.message)) }
        )

    override suspend fun search(query: String): Result<List<Product>> = api.searchAsync(query).awaitCatching(
        success = { it.toResult() },
        failure = { Result.Failure(Exception(it.message)) }
    )


    override suspend fun loadAll(): Result<List<Product>> = api.getAllAsync().awaitCatching(
        success = { it.toResult() },
        failure = { Result.Failure(java.lang.Exception(it.message)) }
    )
}

interface KTORProductAPI {

    @GET("/products/{id}")
    fun getProductAsync(@Path("id") id: String): Deferred<Response<Product>>

    @GET("/products")
    fun getAllAsync(): Deferred<Response<List<Product>>>

    @GET("/products/search/{query}")
    fun searchAsync(@Path("query") query: String): Deferred<Response<List<Product>>>

}