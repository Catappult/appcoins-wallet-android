package com.appcoins.wallet.billing.common

import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.carrierbilling.ForbiddenError
import com.appcoins.wallet.billing.repository.ResponseErrorBaseBody
import com.google.gson.Gson

class BillingErrorMapper(private val gson: Gson) {

  internal companion object {
    internal const val NOT_ALLOWED_CODE = "NotAllowed"
    internal const val FORBIDDEN_CODE = "Authorization.Forbidden"
    internal const val FIELDS_MISSING_CODE = "Body.Fields.Missing"
    internal const val CONFLICT_HTTP_CODE = 409
  }

  fun mapErrorInfo(httpCode: Int?, message: String?): ErrorInfo {
    val messageGson = gson.fromJson(message, ResponseErrorBaseBody::class.java)
    val errorType = getErrorType(httpCode, messageGson.code, messageGson.text)
    return ErrorInfo(httpCode, messageGson.code, messageGson.text, errorType)
  }

  fun mapForbiddenCode(responseCode: String?): ForbiddenError.ForbiddenType? {
    return when (responseCode) {
      NOT_ALLOWED_CODE -> ForbiddenError.ForbiddenType.SUB_ALREADY_OWNED
      FORBIDDEN_CODE -> ForbiddenError.ForbiddenType.BLOCKED
      else -> null
    }
  }

  private fun getErrorType(httpCode: Int?, messageCode: String?,
                           text: String?): ErrorInfo.ErrorType {
    return when {
      httpCode != null && httpCode == 400 && messageCode == FIELDS_MISSING_CODE
          && text?.contains("payment.billing") == true -> ErrorInfo.ErrorType.BILLING_ADDRESS
      messageCode == NOT_ALLOWED_CODE -> ErrorInfo.ErrorType.SUB_ALREADY_OWNED
      messageCode == FORBIDDEN_CODE -> ErrorInfo.ErrorType.BLOCKED
      httpCode == CONFLICT_HTTP_CODE -> ErrorInfo.ErrorType.CONFLICT
      else -> ErrorInfo.ErrorType.UNKNOWN
    }
  }
}
