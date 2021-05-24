package com.asfoundation.wallet.base

/**
 * General specification of common errors used throughout the architecture stack.
 */
sealed class Error {
  sealed class ApiError<out E> : Error() {
    data class HttpError<out E>(val code: Int, val value: E) : ApiError<E>() {
      operator fun invoke(): E = value
    }

    data class NetworkError(val throwable: Throwable) : ApiError<Nothing>()

    data class UnknownError(val throwable: Throwable) : ApiError<Nothing>()
  }

  data class NotFoundError(val throwable: Throwable) : Error()

  data class UnknownError(val throwable: Throwable) : Error()
}