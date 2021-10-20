package com.asfoundation.wallet.eskills.withdraw.repository

import com.appcoins.wallet.billing.util.isNoNetworkException
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import com.google.gson.Gson
import retrofit2.HttpException
import java.math.BigDecimal

class WithdrawApiMapper(private val jsonMapper: Gson) {
  fun map(amount: BigDecimal, error: Throwable): WithdrawResult {
    if (error.isNoNetworkException()) {
      return WithdrawResult(amount, WithdrawResult.Status.NO_NETWORK)
    }
    val response = jsonMapper.fromJson(
      (error as HttpException).response()
        ?.errorBody()
        ?.charStream(),
      Response::class.java
    )
    return when (response.message.code) {
      Status.AMOUNT_NOT_WON -> WithdrawResult(amount, WithdrawResult.Status.NOT_ENOUGH_EARNING)
      Status.NOT_ENOUGH_BALANCE -> WithdrawResult(amount, WithdrawResult.Status.NOT_ENOUGH_BALANCE)
      Status.INVALID_EMAIL -> WithdrawResult(amount, WithdrawResult.Status.INVALID_EMAIL)
      Status.MIN_AMOUNT_REQUIRED -> WithdrawResult(amount, WithdrawResult.Status.MIN_AMOUNT_REQUIRED)
    }
  }

  data class Response(val message: Message)

  data class Message(val detail: String, val code: Status)

  enum class Status {
    AMOUNT_NOT_WON, NOT_ENOUGH_BALANCE, INVALID_EMAIL, MIN_AMOUNT_REQUIRED
  }
}
