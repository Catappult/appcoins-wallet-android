package com.asfoundation.wallet.iab.payment_manager.factory

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.PaymentMethodFactory
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CarrierBillingPaymentMethod

class CarrierBillingFactory : PaymentMethodFactory {

  companion object {
    private const val ID = "carrier_billing"
  }

  override fun create(
    paymentMethodEntity: PaymentMethodEntity,
    purchaseData: PurchaseData,
    productInfoData: ProductInfoData,
    walletInfo: WalletInfo,
    currencyFormatUtils: CurrencyFormatUtils
  ): PaymentMethod? {
    if (paymentMethodEntity.id != ID) return null

    return CarrierBillingPaymentMethod(
      paymentMethod = paymentMethodEntity,
      purchaseData = purchaseData
    )
  }
}