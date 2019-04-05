package com.sdp15.goodb0i.data.store.products

import com.sdp15.goodb0i.data.store.Result
import com.sdp15.goodb0i.data.store.awaitCatching
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

object RetrofitProductLoader : ProductLoader, KoinComponent {

    private val retrofit: Retrofit by inject()

    private val api = retrofit.create(KTORProductAPI::class.java)

    
    
    override suspend fun loadProduct(id: String): Result<Product> = api.getProductAsync(id).awaitCatching(
        success = { Result.Success(it) },
        failure = { Result.Failure(Exception(it.message)) }
    )

    override suspend fun loadCategory(category: String): Result<List<Product>> =
        api.searchAsync(category).awaitCatching(
            success = { Result.Success(it) },
            failure = { Result.Failure(Exception(it.message)) }
        )

    override suspend fun loadProductsForShelfRack(shelfId: Int): Result<List<Product>> =
        api.getProductsForShelfRackAsync(shelfId).awaitCatching(
            success = { Result.Success(it) },
            failure = { Result.Failure(Exception(it.message)) }
        )

    override suspend fun search(query: String): Result<List<Product>> = api.searchAsync(query).awaitCatching(
        success = { Result.Success(it) },
        failure = { Result.Failure(Exception(it.message)) }
    )

    override suspend fun loadAll(): Result<List<Product>> = api.getAllAsync().awaitCatching(
        success = { Result.Success(it) },
        failure = { Result.Failure(Exception(it.message)) }
    )

    override suspend fun searchBarcode(bardcode: String): Result<Product> = api.searchBarcodeAsync(bardcode).awaitCatching(
        success = { Result.Success(it) },
        failure = { Result.Failure(Exception(it.message))}

    )
}

interface KTORProductAPI {

    @GET("/products/{id}")
    fun getProductAsync(@Path("id") id: String): Call<Product>

    @GET("/products/barcode/{barcode}")
    fun searchBarcodeAsync(@Path("barcode") bardcode: String): Call<Product>

    @GET("/products")
    fun getAllAsync(): Call<List<Product>>

    @GET("/products/search/{query}")
    fun searchAsync(@Path("query") query: String): Call<List<Product>>

    @GET("/shelves/{id}")
    fun getProductsForShelfRackAsync(@Path("id") id: Int): Call<List<Product>>

}