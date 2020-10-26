package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.model.payments.response.RedirectAction
import com.adyen.checkout.base.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.base.model.payments.response.Threeds2FingerprintAction
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.*
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.billing.util.getMessage
import com.appcoins.wallet.billing.util.isNoNetworkException
import org.json.JSONObject
import retrofit2.HttpException

class AdyenResponseMapper {

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

    if (adyenResponse?.action != null) {
      actionType = adyenResponse.action.get("type")?.asString
      jsonAction = JSONObject(adyenResponse.action.toString())
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
        adyenResponse?.refusalReasonCode?.toInt(), action, redirectUrl,
        action?.paymentData, response.uid, null, response.hash, response.orderReference,
        response.status, response.metadata?.errorMessage, response.metadata?.errorCode)
  }

  fun map(response: TransactionResponse): PaymentModel {
    return PaymentModel("", null, null, null, "", "", response.uid, response.hash,
        response.orderReference, response.status, response.metadata?.errorMessage,
        response.metadata?.errorCode)
  }

  private fun map(status: TransactionResponse.Status): PaymentModel.Status {
    return when (status) {
      TransactionResponse.Status.PENDING -> PENDING
      TransactionResponse.Status.PENDING_SERVICE_AUTHORIZATION -> PENDING_SERVICE_AUTHORIZATION
      TransactionResponse.Status.SETTLED -> SETTLED
      TransactionResponse.Status.PROCESSING -> PROCESSING
      TransactionResponse.Status.COMPLETED -> COMPLETED
      TransactionResponse.Status.PENDING_USER_PAYMENT -> PENDING_USER_PAYMENT
      TransactionResponse.Status.INVALID_TRANSACTION -> INVALID_TRANSACTION
      TransactionResponse.Status.FAILED -> FAILED
      TransactionResponse.Status.CANCELED -> CANCELED
      TransactionResponse.Status.FRAUD -> FRAUD
    }
  }

  fun map(response: Transaction): PaymentModel {
    return PaymentModel("", null, null, null, "", "",
        response.uid, response.metadata?.purchaseUid,
        response.hash, response.orderReference, map(response.status))
  }

  private fun map(status: Transaction.Status): PaymentModel.Status {
    return when (status) {
      Transaction.Status.PENDING -> PENDING
      Transaction.Status.PENDING_SERVICE_AUTHORIZATION -> PENDING_SERVICE_AUTHORIZATION
      Transaction.Status.SETTLED -> SETTLED
      Transaction.Status.PROCESSING -> PROCESSING
      Transaction.Status.COMPLETED -> COMPLETED
      Transaction.Status.PENDING_USER_PAYMENT -> PENDING_USER_PAYMENT
      Transaction.Status.INVALID_TRANSACTION -> INVALID_TRANSACTION
      Transaction.Status.FAILED -> FAILED
      Transaction.Status.CANCELED -> CANCELED
      Transaction.Status.FRAUD -> FRAUD
    }
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
      message = throwable.getMessage()
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

  companion object {
    const val REDIRECT = "redirect"
    const val THREEDS2FINGERPRINT = "threeDS2Fingerprint"
    const val THREEDS2CHALLENGE = "threeDS2Challenge"
  }
}
