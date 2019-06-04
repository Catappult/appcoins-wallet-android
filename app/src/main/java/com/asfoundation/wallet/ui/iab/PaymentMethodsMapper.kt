package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod
import io.reactivex.exceptions.OnErrorNotImplementedException

class PaymentMethodsMapper {

  fun map(paymentId: String): SelectedPaymentMethod {
    return when (paymentId) {
      "ask_friend" -> SelectedPaymentMethod.SHARE_LINK
      "paypal" -> SelectedPaymentMethod.PAYPAL
      "credit_card" -> SelectedPaymentMethod.CREDIT_CARD
      "alfamart" -> SelectedPaymentMethod.ALFAMART
      "bank_transfer" -> SelectedPaymentMethod.BANK_TRANSFER
      "gopay" -> SelectedPaymentMethod.GOPAY
      "appcoins" -> SelectedPaymentMethod.APPC
      "appcoins_credits" -> SelectedPaymentMethod.APPC_CREDITS
      else -> throw OnErrorNotImplementedException(Throwable("Method not implemented"))
    }
  }

  fun map(selectedPaymentMethod: SelectedPaymentMethod): String {
    return when (selectedPaymentMethod) {
      SelectedPaymentMethod.SHARE_LINK -> "ask_friend"
      SelectedPaymentMethod.PAYPAL -> "paypal"
      SelectedPaymentMethod.CREDIT_CARD -> "credit_card"
      SelectedPaymentMethod.ALFAMART -> "alfamart"
      SelectedPaymentMethod.BANK_TRANSFER -> "bank_transfer"
      SelectedPaymentMethod.GOPAY -> "gopay"
      SelectedPaymentMethod.APPC -> "appcoins"
      SelectedPaymentMethod.APPC_CREDITS -> "appcoins_credits"
    }
  }
}