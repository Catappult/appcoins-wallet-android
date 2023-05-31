package com.appcoins.wallet.core.network.base.call_adapter

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type
import com.appcoins.wallet.core.arch.data.Error

class ApiResultCall<T, E>(private val call: Call<T>,
                          private val successType: Type,
                          private val errorConverter: Converter<ResponseBody, E>) :
    Call<ApiResult<T, E>> {

  override fun enqueue(callback: Callback<ApiResult<T, E>>) {
    call.enqueue(object : Callback<T> {
      override fun onResponse(call: Call<T>, response: Response<T>) {
        callback.onResponse(this@ApiResultCall, Response.success(response.mapToApiResult()))
      }

      override fun onFailure(call: Call<T>, t: Throwable) {
        callback.onResponse(this@ApiResultCall, Response.success(t.mapToApiResult()))
      }
    })
  }

  @Suppress("UNCHECKED_CAST")
  private fun Response<T>.mapToApiResult(): ApiResult<T, E> {
    if (isSuccessful) {
      body()?.let { body -> return Ok(body) }

      return if (successType == Unit::class.java) {
        (Ok(Unit) as ApiResult<T, E>)
      } else {
        Err(Error.ApiError.UnknownError(IllegalStateException("Unexpected null response body.")))
      }
    } else {
      val errorBody = errorBody()
      val convertedBody = if (errorBody == null) {
        return Err(
          Error.ApiError.UnknownError(IllegalStateException("Unexpected null error body."))
        )
      } else {
        try {
          errorConverter.convert(errorBody)!!
        } catch (e: Exception) {
          return Err(
              Error.ApiError.UnknownError(IllegalStateException("Unexpected conversion error."))
          )
        }
      }
      return Err(Error.ApiError.HttpError(code(), convertedBody))
    }
  }

  private fun Throwable.mapToApiResult(): ApiResult<T, E> {
    return Err(
        when (this) {
          is IOException -> Error.ApiError.NetworkError(this)
          else -> Error.ApiError.UnknownError(this)
        }
    )
  }

  override fun execute(): Response<ApiResult<T, E>> {
    throw UnsupportedOperationException("ApiResultCall does not support synchronous execution")
  }

  override fun isExecuted(): Boolean = call.isExecuted

  override fun cancel() = call.cancel()

  override fun isCanceled(): Boolean = call.isCanceled

  override fun request(): Request = call.request()

  override fun timeout(): Timeout = call.timeout()

  override fun clone(): Call<ApiResult<T, E>> {
    return ApiResultCall(call.clone(), successType, errorConverter)
  }
}