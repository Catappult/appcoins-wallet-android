package com.asfoundation.wallet.eskills.withdraw.repository

import com.appcoins.wallet.billing.util.isNoNetworkException
import com.appcoins.wallet.core.utils.android_common.extensions.getMessage
import com.asfoundation.wallet.eskills.withdraw.domain.FailedWithdraw
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException
import java.math.BigDecimal
import javax.inject.Inject

class WithdrawApiMapper @Inject constructor(private val jsonMapper: Gson) {
  fun map(error: Throwable): WithdrawResult {
    return when {
      error.isNoNetworkException() -> FailedWithdraw.NoNetworkError
      error is HttpException -> mapHttpException(error)
      else -> FailedWithdraw.GenericError
    }
  }

  private fun mapHttpException(error: HttpException): WithdrawResult {
    val response = jsonMapper.fromJson(error.getMessage(), Response::class.java)
    return when (response.message.code) {
      Status.AMOUNT_NOT_WON -> FailedWithdraw.NotEnoughEarningError
      Status.NOT_ENOUGH_BALANCE -> FailedWithdraw.NotEnoughBalanceError
      Status.MIN_AMOUNT_REQUIRED -> FailedWithdraw.MinAmountRequiredError(
        response.message.minimumAmount!!
      )
    }
  }

  data class Response(val message: Message)

  data class Message(
    val detail: String, val code: Status,
    @SerializedName("minimum_amount") val minimumAmount: BigDecimal?
  )

  enum class Status {
    AMOUNT_NOT_WON, NOT_ENOUGH_BALANCE, MIN_AMOUNT_REQUIRED
  }
}
