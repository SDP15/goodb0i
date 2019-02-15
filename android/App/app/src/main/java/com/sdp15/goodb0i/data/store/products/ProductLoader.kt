package com.sdp15.goodb0i.data.store.products

import com.sdp15.goodb0i.data.store.Result

interface ProductLoader {

    suspend fun loadProduct(id: String): Result<Product>

    suspend fun loadCategory(category: String): Result<List<Product>>

    suspend fun search(query: String): Result<List<Product>>

    suspend fun loadAll(): Result<List<Product>>

}