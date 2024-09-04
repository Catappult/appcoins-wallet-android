package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import java.math.BigDecimal

open class PaymentMethod(
  open val id: String, open val label: String,
  open val iconUrl: String, val async: Boolean, val fee: PaymentMethodFee?,
  open val isEnabled: Boolean = true, open var disabledReason: Int? = null,
  val showTopup: Boolean = false,
  val showLogout: Boolean = false,
  val showExtraFeesMessage: Boolean = false,
  val price: FiatValue = FiatValue(BigDecimal.ZERO, ""),
  val message: String? = null,
) {
  constructor() : this("", "", "", false, null, false)

  companion object {
    @JvmField
    val APPC: PaymentMethod =
      PaymentMethod(
        "appcoins", "AppCoins (APPC)",
        "https://cdn6.aptoide.com/imgs/a/f/9/af95bd0d14875800231f05dbf1933143_logo.png", false,
        null,
      )
  }

  fun copy(
    id: String = this.id,
    label: String = this.label,
    iconUrl: String = this.iconUrl,
    async: Boolean = this.async,
    fee: PaymentMethodFee? = this.fee,
    isEnabled: Boolean = this.isEnabled,
    disabledReason: Int? = this.disabledReason,
    showTopup: Boolean = this.showTopup,
    showLogout: Boolean = this.showLogout,
    showExtraFeesMessage: Boolean = this.showExtraFeesMessage,
    price: FiatValue = this.price,
    message: String? = this.message
  ): PaymentMethod {
    return PaymentMethod(
      id, label, iconUrl, async, fee, isEnabled, disabledReason,
      showTopup, showLogout, showExtraFeesMessage, price, message
    )
  }

}

data class PaymentMethodFee(
  val isExact: Boolean,
  val amount: BigDecimal?,
  val currency: String?
) {

  fun isValidFee() = isExact && amount != null && currency != null

}