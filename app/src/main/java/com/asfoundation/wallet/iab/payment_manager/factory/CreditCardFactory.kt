package com.asfoundation.wallet.iab.payment_manager.factory

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.PaymentMethodFactory
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod

class CreditCardFactory : PaymentMethodFactory {

  companion object {
    private const val ID = "credit_card"
  }

  override fun create(
    paymentMethodEntity: PaymentMethodEntity,
    purchaseData: PurchaseData,
    walletInfo: WalletInfo,
    currencyFormatUtils: CurrencyFormatUtils
  ): PaymentMethod? {
    if (paymentMethodEntity.id != ID) return null

    return CreditCardPaymentMethod(
      paymentMethod = paymentMethodEntity,
      purchaseData = purchaseData
    )
  }
}
