package com.asfoundation.wallet.iab.payment_manager

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.domain.WalletData
import javax.inject.Inject

class PaymentMethodCreator @Inject constructor(
  private val paymentMethodFactories: List<PaymentMethodFactory>,
  private val currencyFormatUtils: CurrencyFormatUtils,
) {
  fun create(
    paymentMethod: PaymentMethodEntity,
    purchaseData: PurchaseData,
    productInfoData: ProductInfoData,
    walletData: WalletData,
  ): PaymentMethod? {
    return paymentMethodFactories.firstNotNullOfOrNull {
      it.create(
        paymentMethodEntity = paymentMethod,
        purchaseData = purchaseData,
        walletData = walletData,
        productInfoData = productInfoData,
        currencyFormatUtils = currencyFormatUtils
      )
    }
  }
}
