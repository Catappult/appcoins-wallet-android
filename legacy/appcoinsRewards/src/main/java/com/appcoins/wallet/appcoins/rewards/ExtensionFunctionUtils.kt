package com.appcoins.wallet.appcoins.rewards

import java.io.IOException
import retrofit2.HttpException

/** Class file to create kotlin extension functions */
fun Throwable?.isNoNetworkException(): Boolean {
  return this != null && (this is IOException || this.cause != null && this.cause is IOException)
}

fun HttpException.getMessage(): String {
  var message: String? = null
  try {
    val reader = this.response()?.errorBody()?.charStream()
    message = reader?.readText()
    reader?.close()
  } catch (e: Exception) {}
  return if (message.isNullOrBlank()) message() else message
}
