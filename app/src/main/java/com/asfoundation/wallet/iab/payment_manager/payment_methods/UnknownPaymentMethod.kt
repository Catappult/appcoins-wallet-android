package com.asfoundation.wallet.iab.payment_manager.payment_methods

import android.content.Context
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.domain.Transaction
import com.asfoundation.wallet.iab.payment_manager.domain.TransactionData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod

class UnknownPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData
) : PaymentMethod(paymentMethod) {

  override val isEnable: Boolean
    get() = false

  override fun getDescription(): String =
    "Payment method not yet implemented"

  override suspend fun createTransaction(transaction: TransactionData) : Transaction {
    TODO("Not yet implemented")
  }

}
