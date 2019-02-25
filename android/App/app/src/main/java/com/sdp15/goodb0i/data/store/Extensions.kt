package com.sdp15.goodb0i.data.store

import com.sdp15.goodb0i.BuildConfig
import kotlinx.coroutines.Deferred
import retrofit2.Response
import timber.log.Timber

/*
 * Awaits a deferred result, catching any errors
 */
suspend fun <T, U> Deferred<T>.awaitCatching(success: (T) -> U, failure: (Throwable) -> U): U {
    return try {
        success(await())
    } catch (t: Throwable) {
        failure(t)
    }
}
/*
 * Convert a Retrofit2 HTTP Response to a Result
 */
fun <T : Any> Response<T>.toResult(log: Boolean = BuildConfig.DEBUG): Result<T> {
    val body = body()
    return if (isSuccessful && body != null) {
        if (log) Timber.i("Successful response. Body: $body")
        Result.Success(body)
    } else {
        if (log) Timber.e("Failed response. Errorbody ${errorBody()}")
        Result.Failure(APIError(this))
    }
}