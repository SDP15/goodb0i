package com.sdp15.goodb0i.data.store

class PaginatedResult<T>(val data: Collection<T>, val page: Int, val hasNext: Boolean)