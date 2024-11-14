package com.asfoundation.wallet.iab.payment_manager.factory

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.PaymentMethodFactory
import com.asfoundation.wallet.iab.payment_manager.domain.WalletData
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CodaPayPaymentMethod

class CodaPayFactory : PaymentMethodFactory {

  companion object {
    private val IDS = listOf(
      "gopay",
      "ovo",
      "dana",
      "linkaja",
      "doku_wallet",
      "gcash",
      "true_money_wallet",
      "rabbit_line_pay",
      "alfamart",
      "bank_transfer",
      "oxxo",
      "boleto",
      "paytm_upi",
      "paytm_wallet",
      "qiwi",
      "yoo_money",
    )
  }

  override fun create(
    paymentMethodEntity: PaymentMethodEntity,
    purchaseData: PurchaseData,
    productInfoData: ProductInfoData,
    walletData: WalletData,
    currencyFormatUtils: CurrencyFormatUtils
  ): PaymentMethod? {
    if (!IDS.contains(paymentMethodEntity.id)) return null

    return CodaPayPaymentMethod(
      paymentMethod = paymentMethodEntity,
      purchaseData = purchaseData
    )
  }
}
