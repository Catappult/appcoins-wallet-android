package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.appcoins.wallet.billing.util.Error
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
    return PaymentModel(adyenResponse.resultCode, adyenResponse.refusalReason,
        adyenResponse.refusalReasonCode?.toInt(), adyenResponse.action, adyenResponse.action?.url,
        adyenResponse.action?.paymentData, response.uid, response.hash, response.orderReference,
        response.status)
  }

  fun map(response: TransactionResponse): PaymentModel {
    return PaymentModel("", null, null, null, "", "", response.uid, response.hash,
        response.orderReference, response.status)
  }

  fun mapInfoModelError(throwable: Throwable): PaymentInfoModel {
    throwable.printStackTrace()
    val code = if (throwable is HttpException) throwable.code() else null
    return PaymentInfoModel(Error(true, throwable.isNoNetworkException(), code,
        throwable.message))
  }

  fun mapPaymentModelError(throwable: Throwable): PaymentModel {
    throwable.printStackTrace()
    val code = if (throwable is HttpException) throwable.code() else null
    return PaymentModel(Error(true, throwable.isNoNetworkException(), code, throwable.message))
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
}
