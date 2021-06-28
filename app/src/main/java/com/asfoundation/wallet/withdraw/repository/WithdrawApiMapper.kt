package com.asfoundation.wallet.withdraw.repository

import com.appcoins.wallet.billing.util.isNoNetworkException
import com.asfoundation.wallet.withdraw.WithdrawResult
import com.google.gson.Gson
import retrofit2.HttpException

class WithdrawApiMapper {
  fun map(error: Throwable): WithdrawResult {
    if (error.isNoNetworkException()) {
      return WithdrawResult(WithdrawResult.Status.NO_NETWORK)
    }
    val response = Gson().fromJson(
        (error as HttpException).response()
            ?.errorBody()
            ?.charStream(),
        Response::class.java
    )
    return when (response.code) {
      Status.AMOUNT_NOT_WON -> WithdrawResult(WithdrawResult.Status.NOT_ENOUGH_EARNING)
      Status.NOT_ENOUGH_BALANCE -> WithdrawResult(WithdrawResult.Status.NOT_ENOUGH_BALANCE)
    }
  }

  data class Response(val code: Status)

  enum class Status {
    AMOUNT_NOT_WON, NOT_ENOUGH_BALANCE
  }
}
