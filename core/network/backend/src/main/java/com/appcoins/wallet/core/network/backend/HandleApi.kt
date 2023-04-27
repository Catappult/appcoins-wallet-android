package com.appcoins.wallet.core.network.backend

import retrofit2.HttpException
import retrofit2.Response

suspend fun <T : Any> handleApi(execute: suspend () -> Response<T>): ApiResult<T> {
  return try {
    val response = execute()
    val body = response.body()
    if (response.isSuccessful && body != null) {
      ApiSuccess(body)
    } else {
      ApiError(code = response.code(), message = response.message())
    }
  } catch (e: HttpException) {
    ApiError(code = e.code(), message = e.message())
  } catch (e: Throwable) {
    ApiException(e)
  }
}

sealed interface ApiResult<T : Any>
class ApiSuccess<T : Any>(val data: T) : ApiResult<T>
class ApiError<T : Any>(val code: Int, val message: String?) : ApiResult<T>
class ApiException<T : Any>(val e: Throwable) : ApiResult<T>