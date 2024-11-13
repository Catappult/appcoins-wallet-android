package com.asfoundation.wallet.iab.payment_manager.payment_methods

import android.content.Context
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod

class UnknownPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData
) : PaymentMethod(paymentMethod) {

  override val isEnable: Boolean
    get() = false

  override fun getDescription(context: Context): String =
    "Payment method not yet implemented"

  override fun createTransaction() {
    TODO("Not yet implemented")
  }

}
