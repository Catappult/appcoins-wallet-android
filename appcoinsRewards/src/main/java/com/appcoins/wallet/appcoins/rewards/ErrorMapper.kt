package com.appcoins.wallet.appcoins.rewards

import retrofit2.HttpException
import java.net.UnknownHostException

class ErrorMapper {
  companion object {
    private const val FORBIDDEN_CODE = 403
  }

  fun map(throwable: Throwable): Transaction.Status {
    return when (throwable) {
      is UnknownHostException -> Transaction.Status.NO_NETWORK
      is HttpException -> mapHttpException(throwable)
      else -> Transaction.Status.ERROR
    }
  }

  private fun mapHttpException(exception: HttpException): Transaction.Status {
    return if (exception.code() == FORBIDDEN_CODE) {
      Transaction.Status.FORBIDDEN
    } else {
      Transaction.Status.ERROR
    }
  }
}
