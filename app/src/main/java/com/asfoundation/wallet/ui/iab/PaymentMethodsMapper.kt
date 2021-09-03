package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod

class PaymentMethodsMapper(private val billingMessagesMapper: BillingMessagesMapper) {

  fun map(paymentId: String): SelectedPaymentMethod {
    return when (paymentId) {
      "ask_friend" -> SelectedPaymentMethod.SHARE_LINK
      "paypal" -> SelectedPaymentMethod.PAYPAL
      "credit_card" -> SelectedPaymentMethod.CREDIT_CARD
      "appcoins" -> SelectedPaymentMethod.APPC
      "appcoins_credits" -> SelectedPaymentMethod.APPC_CREDITS
      "merged_appcoins" -> SelectedPaymentMethod.MERGED_APPC
      "earn_appcoins" -> SelectedPaymentMethod.EARN_APPC
      "onebip" -> SelectedPaymentMethod.CARRIER_BILLING
      "" -> SelectedPaymentMethod.ERROR
      else -> SelectedPaymentMethod.LOCAL_PAYMENTS
    }
  }

  fun map(selectedPaymentMethod: SelectedPaymentMethod): String {
    return when (selectedPaymentMethod) {
      SelectedPaymentMethod.SHARE_LINK -> "ask_friend"
      SelectedPaymentMethod.PAYPAL -> "paypal"
      SelectedPaymentMethod.CREDIT_CARD -> "credit_card"
      SelectedPaymentMethod.APPC -> "appcoins"
      SelectedPaymentMethod.APPC_CREDITS -> "appcoins_credits"
      SelectedPaymentMethod.MERGED_APPC -> "merged_appcoins"
      SelectedPaymentMethod.LOCAL_PAYMENTS -> "local_payments"
      SelectedPaymentMethod.EARN_APPC -> "earn_appcoins"
      SelectedPaymentMethod.CARRIER_BILLING -> "carrier_billing"
      SelectedPaymentMethod.ERROR -> ""
    }
  }

  fun mapCancellation() = billingMessagesMapper.mapCancellation()

  fun mapFinishedPurchase(purchase: Purchase, itemAlreadyOwned: Boolean): Bundle {
    return billingMessagesMapper.mapFinishedPurchase(purchase, itemAlreadyOwned)
  }
}