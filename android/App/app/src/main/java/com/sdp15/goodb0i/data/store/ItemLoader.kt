package com.sdp15.goodb0i.data.store

interface ItemLoader {

    suspend fun loadItem(id: Long): Result<Item>

    suspend fun loadCategory(category: String): Result<List<Item>>

    suspend fun search(query: String): Result<List<Item>>

    suspend fun loadAll(): Result<List<Item>>

}