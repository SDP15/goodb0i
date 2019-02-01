package com.sdp15.goodb0i.data.store

interface ItemLoader {

    suspend fun loadItem(id: Long): Item

    suspend fun loadCategory(category: String): PaginatedResult<Item>

    suspend fun search(query: String): PaginatedResult<Item>

    suspend fun loadAll(): List<Item>

}