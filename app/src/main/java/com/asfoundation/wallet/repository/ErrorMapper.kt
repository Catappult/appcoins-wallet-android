package com.asfoundation.wallet.repository

import com.appcoins.wallet.appcoins.rewards.getMessage
import com.asfoundation.wallet.repository.PaymentTransaction.PaymentState
import com.asfoundation.wallet.util.UnknownTokenException
import retrofit2.HttpException
import java.net.UnknownHostException

class ErrorMapper {
  fun map(throwable: Throwable): PaymentError {
    throwable.printStackTrace()
    return when (throwable) {
      is HttpException -> mapHttpException(throwable)
      is UnknownHostException -> PaymentError(PaymentState.NO_INTERNET)
      is WrongNetworkException -> PaymentError(PaymentState.WRONG_NETWORK)
      is TransactionNotFoundException -> PaymentError(PaymentState.ERROR)
      is UnknownTokenException -> PaymentError(PaymentState.UNKNOWN_TOKEN)
      is TransactionException -> mapTransactionException(throwable)
      else -> PaymentError(PaymentState.ERROR, null, throwable.message)
    }
  }

  private fun mapTransactionException(throwable: Throwable): PaymentError {
    return when (throwable.message) {
      INSUFFICIENT_ERROR_MESSAGE -> PaymentError(PaymentState.NO_FUNDS)
      NONCE_TOO_LOW_ERROR_MESSAGE -> PaymentError(PaymentState.NONCE_ERROR)
      else -> PaymentError(PaymentState.ERROR, null, throwable.message)
    }
  }

  private fun mapHttpException(exception: HttpException): PaymentError {
    return if (exception.code() == FORBIDDEN_CODE) {
      PaymentError(PaymentState.FORBIDDEN)
    } else {
      val message = exception.getMessage()
      PaymentError(PaymentState.ERROR, exception.code(), message)
    }
  }

  companion object {
    private const val INSUFFICIENT_ERROR_MESSAGE = "insufficient funds for gas * price + value"
    private const val NONCE_TOO_LOW_ERROR_MESSAGE = "nonce too low"
    private const val FORBIDDEN_CODE = 403
  }
}