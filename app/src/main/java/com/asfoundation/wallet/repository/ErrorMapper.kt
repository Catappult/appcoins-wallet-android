package com.asfoundation.wallet.repository

import com.asfoundation.wallet.repository.PaymentTransaction.PaymentState
import com.asfoundation.wallet.util.UnknownTokenException
import retrofit2.HttpException
import java.net.UnknownHostException

class ErrorMapper {
  fun map(throwable: Throwable): PaymentState {
    throwable.printStackTrace()
    return when (throwable) {
      is HttpException -> mapHttpException(throwable)
      is UnknownHostException -> PaymentState.NO_INTERNET
      is WrongNetworkException -> PaymentState.WRONG_NETWORK
      is TransactionNotFoundException -> PaymentState.ERROR
      is UnknownTokenException -> PaymentState.UNKNOWN_TOKEN
      is TransactionException -> mapTransactionException(throwable)
      else -> PaymentState.ERROR
    }
  }

  private fun mapTransactionException(throwable: Throwable): PaymentState {
    return when (throwable.message) {
      INSUFFICIENT_ERROR_MESSAGE -> PaymentState.NO_FUNDS
      NONCE_TOO_LOW_ERROR_MESSAGE -> PaymentState.NONCE_ERROR
      else -> PaymentState.ERROR
    }
  }

  private fun mapHttpException(exception: HttpException): PaymentState {
    return if (exception.code() == FORBIDDEN_CODE) {
      PaymentState.FORBIDDEN
    } else {
      PaymentState.ERROR
    }
  }

  companion object {
    private const val INSUFFICIENT_ERROR_MESSAGE = "insufficient funds for gas * price + value"
    private const val NONCE_TOO_LOW_ERROR_MESSAGE = "nonce too low"
    private const val FORBIDDEN_CODE = 403
  }
}