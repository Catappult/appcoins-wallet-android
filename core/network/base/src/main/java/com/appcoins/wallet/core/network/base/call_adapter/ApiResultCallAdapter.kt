package com.appcoins.wallet.core.network.base.call_adapter

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

class ApiResultCallAdapter<T, E>(private val successType: Type,
                                 private val errorConverter: Converter<ResponseBody, E>)
  : CallAdapter<T, Call<ApiResult<T, E>>> {

  override fun responseType(): Type = successType

  override fun adapt(call: Call<T>): Call<ApiResult<T, E>> {
    return ApiResultCall(call, successType, errorConverter)
  }
}