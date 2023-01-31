package com.appcoins.wallet.billing.common

import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.carrierbilling.ForbiddenError
import com.appcoins.wallet.billing.repository.ResponseErrorBaseBody
import com.google.gson.Gson
import javax.inject.Inject

open class BillingErrorMapper @Inject constructor(private val gson: Gson) {

  internal companion object {
    internal const val NOT_ALLOWED_CODE = "NotAllowed"
    internal const val FORBIDDEN_CODE = "Authorization.Forbidden"
    internal const val FIELDS_MISSING_CODE = "Body.Fields.Missing"
    internal const val ADYEN_V2_ERROR = "AdyenV2.Error"
    internal const val CONFLICT_HTTP_CODE = 409
  }

  open fun mapErrorInfo(httpCode: Int?, message: String?): ErrorInfo {
    val messageGson = gson.fromJson(message, ResponseErrorBaseBody::class.java)
    val errorType = getErrorType(httpCode, messageGson.code, messageGson.text, messageGson.data)
    return ErrorInfo(httpCode, messageGson.code, messageGson.text, errorType)
  }

  open fun mapForbiddenCode(responseCode: String?): ForbiddenError.ForbiddenType? {
    return when (responseCode) {
      NOT_ALLOWED_CODE -> ForbiddenError.ForbiddenType.SUB_ALREADY_OWNED
      FORBIDDEN_CODE -> ForbiddenError.ForbiddenType.BLOCKED
      else -> null
    }
  }

  private fun getErrorType(
    httpCode: Int?, messageCode: String?,
    text: String?, data: Any?
  ): ErrorInfo.ErrorType {
    return when {
      httpCode != null && httpCode == 400 && messageCode == FIELDS_MISSING_CODE
          && text?.contains("payment.billing") == true -> ErrorInfo.ErrorType.BILLING_ADDRESS
      messageCode == NOT_ALLOWED_CODE -> ErrorInfo.ErrorType.SUB_ALREADY_OWNED
      messageCode == FORBIDDEN_CODE -> ErrorInfo.ErrorType.BLOCKED
      httpCode == CONFLICT_HTTP_CODE -> ErrorInfo.ErrorType.CONFLICT
      messageCode == ADYEN_V2_ERROR && (data is Number) -> {
        when (data.toInt()) {
          101 -> {
            ErrorInfo.ErrorType.INVALID_CARD
          }
          103 -> {
            ErrorInfo.ErrorType.CVC_LENGTH
          }
          105 -> {
            ErrorInfo.ErrorType.CARD_SECURITY_VALIDATION
          }
          138 -> {
            ErrorInfo.ErrorType.CURRENCY_NOT_SUPPORTED
          }
          200 -> {
            ErrorInfo.ErrorType.INVALID_COUNTRY_CODE
          }
          172, 174, 422, 800 -> {
            ErrorInfo.ErrorType.OUTDATED_CARD
          }
          704 -> {
            ErrorInfo.ErrorType.ALREADY_PROCESSED
          }
          905 -> {
            ErrorInfo.ErrorType.PAYMENT_ERROR
          }
          907 -> {
            ErrorInfo.ErrorType.PAYMENT_NOT_SUPPORTED_ON_COUNTRY
          }
          else -> ErrorInfo.ErrorType.UNKNOWN
        }
      }
      else -> ErrorInfo.ErrorType.UNKNOWN
    }
  }
}
