package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.model.payments.response.RedirectAction
import com.adyen.checkout.base.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.base.model.payments.response.Threeds2FingerprintAction
import com.appcoins.wallet.billing.util.Error
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class AdyenResponseMapper {

  fun map(response: PaymentMethodsResponse,
          method: AdyenPaymentRepository.Methods): PaymentInfoModel {
    val storedPaymentModel =
        findPaymentMethod(response.payment.storedPaymentMethods, method, true, response.price)
    return if (storedPaymentModel.error.hasError) {
      findPaymentMethod(response.payment.paymentMethods, method, false, response.price)
    } else {
      storedPaymentModel
    }
  }

  fun map(response: AdyenTransactionResponse): PaymentModel {
    val adyenResponse = response.payment
    val actionType = adyenResponse.action?.get("type")?.asString
    val jsonAction = JSONObject(adyenResponse.action.toString())
    var redirectUrl: String? = null
    var action: Action? = null

    if (actionType != null) {
      when (actionType) {
        REDIRECT -> {
          action = RedirectAction.SERIALIZER.deserialize(jsonAction)
          redirectUrl = action.url
        }
        THREEDS2FINGERPRINT -> action = Threeds2FingerprintAction.SERIALIZER.deserialize(jsonAction)

        THREEDS2CHALLENGE -> action = Threeds2ChallengeAction.SERIALIZER.deserialize(jsonAction)
      }
    }
    return PaymentModel(adyenResponse.resultCode, adyenResponse.refusalReason,
        adyenResponse.refusalReasonCode?.toInt(), action, redirectUrl,
        action?.paymentData, response.uid, response.hash, response.orderReference,
        response.status)
  }

  fun map(response: TransactionResponse): PaymentModel {
    return PaymentModel("", null, null, null, "", "", response.uid, response.hash,
        response.orderReference, response.status)
  }

  fun mapInfoModelError(throwable: Throwable): PaymentInfoModel {
    throwable.printStackTrace()
    val codeAndMessage = getErrorCodeAndMessageFromThrowable(throwable)
    return PaymentInfoModel(
        Error(true, throwable.isNoNetworkException(), codeAndMessage.first, codeAndMessage.second))
  }

  fun mapPaymentModelError(throwable: Throwable): PaymentModel {
    throwable.printStackTrace()
    val codeAndMessage = getErrorCodeAndMessageFromThrowable(throwable)
    return PaymentModel(
        Error(true, throwable.isNoNetworkException(), codeAndMessage.first, codeAndMessage.second))
  }

  private fun getErrorCodeAndMessageFromThrowable(throwable: Throwable): Pair<Int?, String?> {
    val code: Int?
    val message: String?
    if (throwable is HttpException) {
      code = throwable.code()
      val retrofitMessage = throwable.response()
          ?.errorBody()
          ?.string()
      if (retrofitMessage.isNullOrBlank()) {
        message = throwable.message
      } else {
        message = retrofitMessage
      }
    } else {
      code = null
      message = throwable.message
    }
    return Pair(code, message)
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

  fun Throwable?.isNoNetworkException(): Boolean {
    return this != null && (this is IOException || (this.cause != null && this.cause is IOException))
  }

  companion object {
    private val REDIRECT = "redirect"
    private val THREEDS2FINGERPRINT = "threeDS2Fingerprint"
    private val THREEDS2CHALLENGE = "threeDS2Challenge"
  }
}
