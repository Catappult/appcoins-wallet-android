package com.asfoundation.wallet.iab.payment_manager.payment_methods

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod

class APPCPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData,
  private val productInfoData: ProductInfoData,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val walletInfo: WalletInfo
) : PaymentMethod(paymentMethod) {

  override val onBuyClick: () -> Unit
    get() = { }

  override fun createTransaction() {
    TODO("Not yet implemented")
  }

}
