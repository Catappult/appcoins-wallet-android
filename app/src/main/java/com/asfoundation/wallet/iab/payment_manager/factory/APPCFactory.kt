package com.asfoundation.wallet.iab.payment_manager.factory

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.PaymentMethodFactory
import com.asfoundation.wallet.iab.payment_manager.payment_methods.APPCPaymentMethod

class APPCFactory : PaymentMethodFactory {

  companion object {
    private const val ID = "appcoins_credits"
  }

  override fun create(
    paymentMethodEntity: PaymentMethodEntity,
    purchaseData: PurchaseData,
    walletInfo: WalletInfo,
    currencyFormatUtils: CurrencyFormatUtils
  ): PaymentMethod? {
    if (paymentMethodEntity.id != ID) return null

    return APPCPaymentMethod(
      paymentMethod = paymentMethodEntity,
      purchaseData = purchaseData,
      walletInfo = walletInfo,
      currencyFormatUtils = currencyFormatUtils
    )
  }
}
