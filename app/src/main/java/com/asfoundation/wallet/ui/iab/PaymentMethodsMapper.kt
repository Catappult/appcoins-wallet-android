package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod
import io.reactivex.exceptions.OnErrorNotImplementedException

class PaymentMethodsMapper {

  fun map(paymentId: String): SelectedPaymentMethod {
    when (paymentId) {
      "ask_friend" -> return SelectedPaymentMethod.SHARE_LINK
      "paypal" -> return SelectedPaymentMethod.PAYPAL
      "credit_card" -> return SelectedPaymentMethod.CREDIT_CARD
      "alfamart" -> return SelectedPaymentMethod.ALFAMART
      "bank_transfer" -> return SelectedPaymentMethod.BANK_TRANSFER
      "gopay" -> return SelectedPaymentMethod.GOPAY
      "appcoins" -> return SelectedPaymentMethod.APPC
      "appcoins_credits" -> return SelectedPaymentMethod.APPC_CREDITS
      else -> throw OnErrorNotImplementedException(Throwable("Method not implemented"))
    }
  }

}