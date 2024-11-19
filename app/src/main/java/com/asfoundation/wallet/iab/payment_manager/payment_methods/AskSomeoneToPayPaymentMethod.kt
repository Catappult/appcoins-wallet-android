package com.asfoundation.wallet.iab.payment_manager.payment_methods

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.domain.Transaction
import com.asfoundation.wallet.iab.payment_manager.domain.TransactionData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod

class AskSomeoneToPayPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData
) : PaymentMethod(paymentMethod) {

  override suspend fun createTransaction(transaction: TransactionData): Transaction {
    TODO("Not yet implemented")
  }

}
