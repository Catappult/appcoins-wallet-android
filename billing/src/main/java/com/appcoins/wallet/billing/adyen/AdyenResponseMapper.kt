package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.model.payments.response.RedirectAction
import com.adyen.checkout.base.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.base.model.payments.response.Threeds2FingerprintAction
import com.appcoins.wallet.billing.common.response.TransactionResponse
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.billing.util.getErrorCodeAndMessage
import com.appcoins.wallet.billing.util.getMessage
import com.appcoins.wallet.billing.util.isNoNetworkException
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.HttpException

class AdyenResponseMapper(private val gson: Gson) {

  fun map(response: PaymentMethodsResponse,
          method: AdyenPaymentRepository.Methods): PaymentInfoModel {
    //This was done due to the fact that using the PaymentMethodsApiResponse to map the response
    // directly with retrofit was breaking when the response came with a configuration object
    // since the Adyen lib considers configuration a string.
    val adyenResponse: PaymentMethodsApiResponse =
        PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(response.payment.toString()))
    val storedPaymentModel =
        findPaymentMethod(adyenResponse.storedPaymentMethods, method, true, response.price)
    return if (storedPaymentModel.error.hasError) {
      findPaymentMethod(adyenResponse.paymentMethods, method, false, response.price)
    } else {
      storedPaymentModel
    }
  }

  fun map(response: AdyenTransactionResponse): PaymentModel {
    val adyenResponse = response.payment
    var actionType: String? = null
    var jsonAction: JSONObject? = null
    var redirectUrl: String? = null
    var action: Action? = null
    var fraudResultsId: List<Int> = emptyList()

    if (adyenResponse != null) {
      if (adyenResponse.fraudResult != null) {
        fraudResultsId = adyenResponse.fraudResult.results.map { it.fraudCheckResult.checkId }
      }
      if (adyenResponse.action != null) {
        actionType = adyenResponse.action.get("type")?.asString
        jsonAction = JSONObject(adyenResponse.action.toString())
      }
    }

    if (actionType != null && jsonAction != null) {
      when (actionType) {
        REDIRECT -> {
          action = RedirectAction.SERIALIZER.deserialize(jsonAction)
          redirectUrl = action.url
        }
        THREEDS2FINGERPRINT -> action = Threeds2FingerprintAction.SERIALIZER.deserialize(jsonAction)
        THREEDS2CHALLENGE -> action = Threeds2ChallengeAction.SERIALIZER.deserialize(jsonAction)
      }
    }
    return PaymentModel(adyenResponse?.resultCode, adyenResponse?.refusalReason,
        adyenResponse?.refusalReasonCode?.toInt(), action, redirectUrl, action?.paymentData,
        response.uid, response.hash, response.orderReference, fraudResultsId, response.status,
        response.metadata?.errorMessage, response.metadata?.errorCode)
  }

  fun map(response: TransactionResponse): PaymentModel {
    return PaymentModel(response)
  }

  fun mapInfoModelError(throwable: Throwable): PaymentInfoModel {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    return PaymentInfoModel(
        Error(true, throwable.isNoNetworkException(), codeAndMessage.first, codeAndMessage.second))
  }

  fun mapPaymentModelError(throwable: Throwable): PaymentModel {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    var error =
        Error(true, throwable.isNoNetworkException(), codeAndMessage.first, codeAndMessage.second)

    if (error.message?.contains("payment.billing_address") == false && throwable is HttpException) {
      val adyenErrorResponse = gson.fromJson(codeAndMessage.second, AdyenErrorResponse::class.java)
      if (adyenErrorResponse.code == "AdyenV2.Error") {
        when (adyenErrorResponse.data) {
          101 -> {
            error = Error(true, throwable.isNoNetworkException(), codeAndMessage.first,
                codeAndMessage.second, Error.ErrorType.INVALID_CARD)
          }
          105 -> {
            error = Error(true, throwable.isNoNetworkException(), codeAndMessage.first,
                codeAndMessage.second, Error.ErrorType.CARD_SECURITY_VALIDATION)
          }
          172 -> {

            error = Error(true, throwable.isNoNetworkException(), codeAndMessage.first,
                codeAndMessage.second, Error.ErrorType.TIMEOUT)
          }
          704 -> {
            error = Error(true, throwable.isNoNetworkException(), codeAndMessage.first,
                codeAndMessage.second, Error.ErrorType.ALREADY_PROCESSED)
          }
          905 -> {
            error = Error(true, throwable.isNoNetworkException(), codeAndMessage.first,
                codeAndMessage.second, Error.ErrorType.PAYMENT_ERROR)
          }
        }
      }
    }
    return PaymentModel(error)
  }

  fun mapVerificationPaymentModeSuccess(): VerificationPaymentModel {
    return VerificationPaymentModel(true, null, null, null)
  }

  fun mapVerificationPaymentModelError(throwable: Throwable): VerificationPaymentModel {
    throwable.printStackTrace()
    return if (throwable is HttpException) {
      val body = throwable.getMessage()
      val verificationTransactionResponse =
          gson.fromJson(body, VerificationTransactionResponse::class.java)
      var errorType = VerificationPaymentModel.ErrorType.OTHER
      when (verificationTransactionResponse.code) {
        "Request.Invalid" -> errorType = VerificationPaymentModel.ErrorType.INVALID_REQUEST
        "Request.TooMany" -> errorType = VerificationPaymentModel.ErrorType.TOO_MANY_ATTEMPTS
      }
      VerificationPaymentModel(false, errorType,
          verificationTransactionResponse.data?.refusalReason,
          verificationTransactionResponse.data?.refusalReasonCode?.toInt(), Error(hasError = true,
          isNetworkError = false))
    } else {
      val codeAndMessage = throwable.getErrorCodeAndMessage()
      VerificationPaymentModel(false, VerificationPaymentModel.ErrorType.OTHER, null, null,
          Error(true, throwable.isNoNetworkException(), codeAndMessage.first,
              codeAndMessage.second))
    }
  }

  fun mapVerificationCodeError(throwable: Throwable): VerificationCodeResult {
    throwable.printStackTrace()
    if (throwable is HttpException) {
      val body = throwable.getMessage()
      val verificationTransactionResponse =
          gson.fromJson(body, VerificationErrorResponse::class.java)
      var errorType = VerificationCodeResult.ErrorType.OTHER
      when (verificationTransactionResponse.code) {
        "Body.Invalid" -> errorType = VerificationCodeResult.ErrorType.WRONG_CODE
        "Request.TooMany" -> errorType = VerificationCodeResult.ErrorType.TOO_MANY_ATTEMPTS
      }
      return VerificationCodeResult(false, errorType, Error(hasError = true,
          isNetworkError = true, code = throwable.code(), message = body))
    }
    return VerificationCodeResult(success = false,
        errorType = VerificationCodeResult.ErrorType.OTHER,
        error = Error(hasError = true, isNetworkError = false, code = null,
            message = throwable.message))
  }

  private fun findPaymentMethod(paymentMethods: List<PaymentMethod>?,
                                method: AdyenPaymentRepository.Methods,
                                isStored: Boolean, price: Price): PaymentInfoModel {
    paymentMethods?.let {
      for (paymentMethod in it) {
        if (paymentMethod.type == method.adyenType) return PaymentInfoModel(paymentMethod, isStored,
            price.value, price.currency)
      }
    }
    return PaymentInfoModel(Error(true))
  }

  companion object {
    const val REDIRECT = "redirect"
    const val THREEDS2FINGERPRINT = "threeDS2Fingerprint"
    const val THREEDS2CHALLENGE = "threeDS2Challenge"
  }
}
