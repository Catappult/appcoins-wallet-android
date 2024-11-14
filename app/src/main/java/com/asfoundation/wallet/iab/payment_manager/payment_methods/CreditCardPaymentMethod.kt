package com.asfoundation.wallet.iab.payment_manager.payment_methods

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.network.microservices.model.emptyPaymentMethodEntity
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.model.emptyPurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.domain.WalletData
import com.asfoundation.wallet.iab.payment_manager.domain.emptyWalletData

class CreditCardPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData,
  private val walletData: WalletData,
) : PaymentMethod(paymentMethod) {

  override fun createTransaction() {
    TODO("Not yet implemented")
  }

}

val emptyCreditCardPaymentMethod = CreditCardPaymentMethod(
  paymentMethod = emptyPaymentMethodEntity,
  purchaseData = emptyPurchaseData,
  walletData = emptyWalletData,
)
