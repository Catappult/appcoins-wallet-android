package com.asfoundation.wallet.repository

import com.appcoins.wallet.appcoins.rewards.ResponseErrorBaseBody
import com.appcoins.wallet.appcoins.rewards.getMessage
import com.asfoundation.wallet.repository.PaymentTransaction.PaymentState
import com.appcoins.wallet.core.utils.jvm_common.UnknownTokenException
import com.google.gson.Gson
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject

class PaymentErrorMapper @Inject constructor(private val gson: Gson) {

  fun map(throwable: Throwable): PaymentError {
    throwable.printStackTrace()
    return when (throwable) {
      is HttpException -> mapHttpException(throwable)
      is UnknownHostException -> PaymentError(PaymentState.NO_INTERNET)
      is WrongNetworkException -> PaymentError(PaymentState.WRONG_NETWORK)
      is TransactionNotFoundException -> PaymentError(PaymentState.ERROR)
      is com.appcoins.wallet.core.utils.jvm_common.UnknownTokenException -> PaymentError(PaymentState.UNKNOWN_TOKEN)
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
      val messageInfo = gson.fromJson(exception.getMessage(), ResponseErrorBaseBody::class.java)
      when (messageInfo.code) {
        "NotAllowed" -> PaymentError(PaymentState.SUB_ALREADY_OWNED)
        "Authorization.Forbidden" -> PaymentError(PaymentState.FORBIDDEN)
        else -> PaymentError(PaymentState.ERROR)
      }
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