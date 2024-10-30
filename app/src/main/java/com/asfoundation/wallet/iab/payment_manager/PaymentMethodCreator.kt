package com.asfoundation.wallet.iab.payment_manager

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import javax.inject.Inject

class PaymentMethodCreator @Inject constructor(
  private val paymentMethodFactories: List<PaymentMethodFactory>,
  private val currencyFormatUtils: CurrencyFormatUtils,
) {
  fun create(
    paymentMethod: PaymentMethodEntity,
    purchaseData: PurchaseData,
    productInfoData: ProductInfoData,
    walletInfo: WalletInfo,
  ): PaymentMethod? {
    return paymentMethodFactories.firstNotNullOfOrNull {
      it.create(
        paymentMethodEntity = paymentMethod,
        purchaseData = purchaseData,
        walletInfo = walletInfo,
        productInfoData = productInfoData,
        currencyFormatUtils = currencyFormatUtils
      )
    }
  }
}
