package com.asfoundation.wallet.iab.payment_manager

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.asfoundation.wallet.iab.domain.model.PurchaseData

interface PaymentMethodFactory {

  fun create(
    paymentMethodEntity: PaymentMethodEntity,
    purchaseData: PurchaseData,
    walletInfo: WalletInfo,
    currencyFormatUtils: CurrencyFormatUtils
  ): PaymentMethod?
}
