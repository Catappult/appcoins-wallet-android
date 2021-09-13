package com.asfoundation.wallet.withdraw.repository

import com.appcoins.wallet.billing.util.isNoNetworkException
import com.asfoundation.wallet.withdraw.WithdrawResult
import com.google.gson.Gson
import retrofit2.HttpException

class WithdrawApiMapper(private val jsonMapper: Gson) {
  fun map(error: Throwable): WithdrawResult {
    if (error.isNoNetworkException()) {
      return WithdrawResult(WithdrawResult.Status.NO_NETWORK)
    }
    val response = jsonMapper.fromJson(
      (error as HttpException).response()
        ?.errorBody()
        ?.charStream(),
      Response::class.java
    )
    return when (response.code) {
      Status.AMOUNT_NOT_WON -> WithdrawResult(WithdrawResult.Status.NOT_ENOUGH_EARNING)
      Status.NOT_ENOUGH_BALANCE -> WithdrawResult(WithdrawResult.Status.NOT_ENOUGH_BALANCE)
      Status.INVALID_EMAIL -> WithdrawResult(WithdrawResult.Status.INVALID_EMAIL)
    }
  }

  data class Response(val code: Status)

  enum class Status {
    AMOUNT_NOT_WON, NOT_ENOUGH_BALANCE, INVALID_EMAIL
  }
}
