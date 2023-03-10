package com.asfoundation.wallet.redeem_gift.repository

import com.appcoins.wallet.billing.util.isNoNetworkException
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject

class RedeemGiftMapper @Inject constructor(private val jsonMapper: Gson) {
  fun map(error: Throwable): RedeemCode {
    return when {
      error.isNoNetworkException() -> FailedRedeem.GenericError("")
      error is HttpException -> mapHttpException(error)
      else -> FailedRedeem.GenericError(error.toString())
    }
  }

  private fun mapHttpException(error: HttpException): RedeemCode {
    return when (error.code()) {
      450 -> FailedRedeem.AlreadyRedeemedError
      451 -> FailedRedeem.OnlyNewUsersError
      else -> FailedRedeem.GenericError(error.message())
    }
  }

  data class Response(val message: Message)

  data class Message(
    val detail: String, val code: String
  )

}
