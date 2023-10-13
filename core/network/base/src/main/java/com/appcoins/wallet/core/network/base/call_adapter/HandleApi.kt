package com.appcoins.wallet.core.network.base.call_adapter

import retrofit2.HttpException
import retrofit2.Response

suspend fun <T : Any> handleApi(execute: suspend () -> Response<T>): Result<T> {
  return try {
    val response = execute()
    val body = response.body()
    if (response.isSuccessful && body != null) {
      ApiSuccess(body)
    } else {
      ApiFailure(code = response.code(), message = response.message())
    }
  } catch (e: HttpException) {
    ApiFailure(code = e.code(), message = e.message())
  } catch (e: Throwable) {
    ApiException(e)
  }
}

sealed interface Result<T : Any>
class ApiSuccess<T : Any>(val data: T) :
  Result<T>

class ApiFailure<T : Any>(val code: Int, val message: String?) :
  Result<T>

class ApiException<T : Any>(val e: Throwable) :
  Result<T>