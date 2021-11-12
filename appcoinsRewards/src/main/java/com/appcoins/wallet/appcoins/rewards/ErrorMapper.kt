package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.ErrorInfo.ErrorType
import com.google.gson.Gson
import retrofit2.HttpException

class ErrorMapper(private val gson: Gson) {
  companion object {
    private const val FORBIDDEN_CODE = 403
  }

  fun map(throwable: Throwable): ErrorInfo {
    return when {
      throwable.isNoNetworkException() -> ErrorInfo(ErrorType.NO_NETWORK, null, null)
      throwable is HttpException -> mapHttpException(throwable)
      else -> ErrorInfo(ErrorType.UNKNOWN, null, throwable.message)
    }
  }

  private fun mapHttpException(exception: HttpException): ErrorInfo {
    return if (exception.code() == FORBIDDEN_CODE) {
      try {
        val messageInfo = gson.fromJson(exception.getMessage(), ResponseErrorBaseBody::class.java)
        when (messageInfo.code) {
          "NotAllowed" -> ErrorInfo(ErrorType.SUB_ALREADY_OWNED, null, null)
          "Authorization.Forbidden" -> ErrorInfo(ErrorType.BLOCKED, null, null)
          else -> ErrorInfo(ErrorType.UNKNOWN, exception.code(), messageInfo.text)
        }
      } catch (e: Exception) {
        ErrorInfo(ErrorType.UNKNOWN, null, null)
      }
    } else {
      val message = exception.getMessage()
      ErrorInfo(ErrorType.UNKNOWN, exception.code(), message)
    }
  }
}

data class ResponseErrorBaseBody(val code: String?, val path: String?, val text: String?,
                                 val data: Any?)