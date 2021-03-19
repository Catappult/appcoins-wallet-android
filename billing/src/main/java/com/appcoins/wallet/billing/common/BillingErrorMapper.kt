package com.appcoins.wallet.billing.common

import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.carrierbilling.ForbiddenError
import com.appcoins.wallet.billing.repository.ResponseErrorBaseBody
import com.google.gson.Gson

class BillingErrorMapper(private val gson: Gson) {

  fun mapErrorInfo(httpCode: Int?, message: String?): ErrorInfo {
    val messageGson = gson.fromJson(message, ResponseErrorBaseBody::class.java)
    val errorType = getErrorType(httpCode, messageGson.code, messageGson.text)
    return ErrorInfo(httpCode, messageGson.code, messageGson.text, errorType)
  }

  fun mapForbiddenCode(responseCode: String?): ForbiddenError.ForbiddenType? {
    return when (responseCode) {
      "NotAllowed" -> ForbiddenError.ForbiddenType.SUB_ALREADY_OWNED
      "Authorization.Forbidden" -> ForbiddenError.ForbiddenType.BLOCKED
      else -> null
    }
  }

  private fun getErrorType(httpCode: Int?, messageCode: String?,
                           text: String?): ErrorInfo.ErrorType {
    return when {
      httpCode != null && httpCode == 400 && messageCode == "Body.Fields.Missing"
          && text?.contains("payment.billing") == true -> ErrorInfo.ErrorType.BILLING_ADDRESS
      messageCode == "NotAllowed" -> ErrorInfo.ErrorType.SUB_ALREADY_OWNED
      messageCode == "Authorization.Forbidden" -> ErrorInfo.ErrorType.BLOCKED
      httpCode == 409 -> ErrorInfo.ErrorType.CONFLICT
      else -> ErrorInfo.ErrorType.UNKNOWN
    }
  }
}
