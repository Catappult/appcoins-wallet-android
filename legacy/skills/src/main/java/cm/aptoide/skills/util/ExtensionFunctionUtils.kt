package cm.aptoide.skills.util

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

fun Throwable.getErrorCodeAndMessage(): Pair<Int?, String?> {
  val code: Int?
  val message: String?
  if (this is HttpException) {
    code = this.code()
    message = this.getMessage()
  } else {
    code = null
    message = this.message
  }
  return Pair(code, message)
}

fun HttpException.getMessage(): String {
  var message: String? = null
  try {
    val reader = this.response()
      ?.errorBody()
      ?.charStream()
    message = reader?.readText()
    reader?.close()
  } catch (e: Exception) {
  }
  return if (message.isNullOrBlank()) message() else message
}