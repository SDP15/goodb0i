package com.sdp15.goodb0i.data.store

import kotlinx.coroutines.Deferred



interface ListLoader {

    suspend fun loadList(code: Long): Result<List<ListItem>>

    suspend fun createList(contents: List<Pair<String, Int>>): Result<String>
}