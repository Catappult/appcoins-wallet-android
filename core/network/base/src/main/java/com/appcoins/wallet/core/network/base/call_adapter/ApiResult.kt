package com.appcoins.wallet.core.network.base.call_adapter

import com.appcoins.wallet.core.network.base.HttpCodes
import com.appcoins.wallet.ui.arch.data.Error
import com.github.michaelbull.result.Result

typealias ApiResult<T, E> = Result<T, Error.ApiError<E>>

fun <E> Error.ApiError.HttpError<E>.isSuccess(): Boolean {
  return HttpCodes.isSuccess(code)
}

fun <E> Error.ApiError.HttpError<E>.isServerError(): Boolean {
  return HttpCodes.isServerError(code)
}

fun <E> Error.ApiError.HttpError<E>.isClientError(): Boolean {
  return HttpCodes.isClientError(code)
}
