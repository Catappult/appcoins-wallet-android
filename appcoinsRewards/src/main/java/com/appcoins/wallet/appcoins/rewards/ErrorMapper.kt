package com.appcoins.wallet.appcoins.rewards

import retrofit2.HttpException

class ErrorMapper {
  companion object {
    private const val FORBIDDEN_CODE = 403
  }

  fun map(throwable: Throwable): TransactionError {
    return when {
      throwable.isNoNetworkException() -> TransactionError(Transaction.Status.NO_NETWORK, null,
          null)
      throwable is HttpException -> mapHttpException(throwable)
      else -> TransactionError(Transaction.Status.ERROR, null, throwable.message)
    }
  }

  private fun mapHttpException(exception: HttpException): TransactionError {
    return if (exception.code() == FORBIDDEN_CODE) {
      TransactionError(Transaction.Status.FORBIDDEN, null, null)
    } else {
      val message = exception.getMessage()
      TransactionError(Transaction.Status.ERROR, exception.code(), message)
    }
  }
}
