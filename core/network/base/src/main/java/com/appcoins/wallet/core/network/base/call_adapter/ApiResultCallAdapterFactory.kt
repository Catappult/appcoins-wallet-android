package com.appcoins.wallet.core.network.base.call_adapter

import com.appcoins.wallet.core.arch.data.Error
import com.github.michaelbull.result.Result
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiResultCallAdapterFactory : CallAdapter.Factory() {

  override fun get(
    returnType: Type, annotations: Array<out Annotation>,
    retrofit: Retrofit
  ): CallAdapter<*, *>? {
    if (getRawType(returnType) != Call::class.java) return null
    check(
      returnType is ParameterizedType
    ) { "$returnType must be parameterized. Raw types are not supported" }

    val resultType = getParameterUpperBound(0, returnType)
    if (getRawType(resultType) != Result::class.java) return null
    check(
      resultType is ParameterizedType
    ) { "$returnType must be parameterized. Raw types are not supported" }

    val successBodyType = getParameterUpperBound(0, resultType)
    val errorBodyType = getParameterUpperBound(1, resultType)

    if (getRawType(errorBodyType) != Error.ApiError::class.java) return null
    check(
      errorBodyType is ParameterizedType
    ) { "ApiFail must be parameterized. Raw types are not supported" }

    val errorInnerBodyType = getParameterUpperBound(0, errorBodyType)

    val errorBodyConverter =
      retrofit.nextResponseBodyConverter<Any>(null, errorInnerBodyType, annotations)
    return ApiResultCallAdapter<Any, Any>(successBodyType, errorBodyConverter)
  }
}