package com.asfoundation.wallet.iab.payment_manager.payment_methods

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod

class CreditCardPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData,
) : PaymentMethod(paymentMethod) {

  override val onBuyClick: () -> Unit
    get() = { }

  override fun createTransaction() {
    TODO("Not yet implemented")
  }

}
