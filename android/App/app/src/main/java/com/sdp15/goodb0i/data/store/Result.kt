package com.sdp15.goodb0i.data.store

sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Failure<out T : Any>(val exception: Exception) : Result<T>()
    //data  class PaginatedResult<out T: Any>(val data: Collection<T>, val page: Int, val hasNext: Boolean): Result<T>()


}