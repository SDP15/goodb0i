package com.sdp15.goodb0i.data.store.lists

import com.sdp15.goodb0i.data.store.Result

interface ListManager {

    suspend fun loadList(code: Long): Result<ShoppingList>

    suspend fun createList(contents: List<Pair<String, Int>>): Result<String>
}