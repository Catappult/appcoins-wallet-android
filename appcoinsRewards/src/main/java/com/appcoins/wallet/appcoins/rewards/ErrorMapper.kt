package com.appcoins.wallet.appcoins.rewards

import java.net.UnknownHostException

class ErrorMapper {
  fun map(throwable: Throwable): Transaction.Status {
    return when (throwable) {
      is UnknownHostException ->
        Transaction.Status.NO_NETWORK
      else -> {
        Transaction.Status.ERROR
      }
    }
  }
}
