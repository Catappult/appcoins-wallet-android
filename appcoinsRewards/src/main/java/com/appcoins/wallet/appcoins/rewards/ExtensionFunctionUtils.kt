package com.appcoins.wallet.appcoins.rewards

import retrofit2.HttpException
import java.io.IOException

/**
 *
 * Class file to create kotlin extension functions
 *
 */

fun Throwable?.isNoNetworkException(): Boolean {
  return this != null && (this is IOException || this.cause != null && this.cause is IOException)
}

fun HttpException.getMessage(): String {
  val reader = this.response()
      ?.errorBody()
      ?.charStream()
  val message = reader?.readText()
  reader?.close()
  return if (message.isNullOrBlank()) message() else message
}