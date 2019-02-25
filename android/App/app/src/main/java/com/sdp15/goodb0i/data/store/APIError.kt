package com.sdp15.goodb0i.data.store

import retrofit2.Response

class APIError(private val status: Int, val text: String) : Exception() {
    constructor(response: Response<*>) : this(response.code(), response.errorBody()?.string() ?: "")

    override val message: String?
        get() = "Status: $status. Text: $text"
}