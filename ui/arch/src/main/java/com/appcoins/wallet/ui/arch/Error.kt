package com.appcoins.wallet.ui.arch

/**
 * General specification of common errors used throughout the architecture stack.
 */
sealed class Error(open val throwable: Throwable) {
  sealed class ApiError<out E>(override val throwable: Throwable) : Error(throwable) {
    data class HttpError<out E>(val code: Int, val value: E) :
        ApiError<E>(Throwable("HttpError - Code:${code} Value:${value}")) {
      operator fun invoke(): E = value
    }

    data class NetworkError(override val throwable: Throwable) : ApiError<Nothing>(throwable)
    data class UnknownError(override val throwable: Throwable) : ApiError<Nothing>(throwable)
  }

  data class NotFoundError(override val throwable: Throwable) : Error(throwable)
  data class UnknownError(override val throwable: Throwable) : Error(throwable)
}