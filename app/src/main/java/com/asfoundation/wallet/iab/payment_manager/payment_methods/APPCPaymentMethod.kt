package com.asfoundation.wallet.iab.payment_manager.payment_methods

import android.content.Context
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import java.math.BigDecimal

class APPCPaymentMethod(
  paymentMethod: PaymentMethodEntity,
  private val purchaseData: PurchaseData,
  private val productInfoData: ProductInfoData,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val walletInfo: WalletInfo
) : PaymentMethod(paymentMethod) {

  private val hasFunds
    get() = walletInfo.walletBalance.creditsBalance.fiat.amount >= paymentMethod.price.value

  override val isEnable: Boolean
    get() = paymentMethod.isAvailable() && hasFunds
  override val cost: BigDecimal
    get() = productInfoData.transaction.amount
  override val taxes: BigDecimal?
    get() = null
  override val subtotal: BigDecimal?
    get() = null
  override val currency: String
    get() = productInfoData.transaction.currency
  override val hasFees: Boolean
    get() = false

  override val onBuyClick: () -> Unit
    get() = { }

  override fun getDescription(context: Context) =
    walletInfo.walletBalance.creditsBalance.fiat.run {
      "$symbol ${currencyFormatUtils.formatCurrency(amount)}"
    }
      .takeIf { hasFunds }
      ?: walletInfo.walletBalance.creditsBalance.fiat.run {
        "Insufficient funds: $symbol ${currencyFormatUtils.formatCurrency(amount)}"
      }

  override fun createTransaction() {
    TODO("Not yet implemented")
  }

}
