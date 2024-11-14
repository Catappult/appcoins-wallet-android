package com.asfoundation.wallet.iab.payment_manager

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.domain.WalletData

interface PaymentMethodFactory {

  fun create(
    paymentMethodEntity: PaymentMethodEntity,
    purchaseData: PurchaseData,
    productInfoData: ProductInfoData,
    walletData: WalletData,
    currencyFormatUtils: CurrencyFormatUtils
  ): PaymentMethod?
}
