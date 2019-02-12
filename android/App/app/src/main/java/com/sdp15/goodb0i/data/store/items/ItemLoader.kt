package com.sdp15.goodb0i.data.store.items

import com.sdp15.goodb0i.data.store.Result

interface ItemLoader {

    suspend fun loadItem(id: String): Result<Item>

    suspend fun loadCategory(category: String): Result<List<Item>>

    suspend fun search(query: String): Result<List<Item>>

    suspend fun loadAll(): Result<List<Item>>

}