package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.util.Error
import java.io.IOException

class AdyenResponseMapper {

  fun map(response: PaymentMethodsResponse,
          method: AdyenPaymentService.Methods): PaymentInfoModel {
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
    return PaymentModel(adyenResponse.resultCode, adyenResponse.refusalReason,
        adyenResponse.refusalReasonCode?.toInt(), adyenResponse.action, adyenResponse.action?.url,
        adyenResponse.action?.paymentData)
  }

  fun mapInfoModelError(throwable: Throwable): PaymentInfoModel {
    return PaymentInfoModel(Error(true, throwable.isNoNetworkException()))
  }

  fun mapModelError(throwable: Throwable): PaymentModel {
    return PaymentModel(Error(true, throwable.isNoNetworkException()))
  }

  private fun findPaymentMethod(paymentMethods: List<PaymentMethod>?,
                                method: AdyenPaymentService.Methods,
                                isStored: Boolean, price: Price): PaymentInfoModel {
    paymentMethods?.let {
      for (paymentMethod in it) {
        if (paymentMethod.type == method.type) return PaymentInfoModel(paymentMethod, isStored,
            price)
      }
    }
    return PaymentInfoModel(Error(true))
  }

  fun Throwable?.isNoNetworkException(): Boolean {
    return this != null && (this is IOException || this.cause != null && this.cause is IOException)
  }
}
