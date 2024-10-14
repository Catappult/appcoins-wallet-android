package com.asfoundation.wallet.billing.adyen

import androidx.annotation.DrawableRes
import com.appcoins.wallet.ui.common.R

enum class PaymentBrands(val brandName: String, @DrawableRes val brandFlag: Int) {
  MASTERCARD("mc", R.drawable.ic_card_brand_master_card),
  VISA("visa", R.drawable.ic_card_brand_visa),
  AMEX("amex", R.drawable.ic_card_brand_american_express),
  MAESTRO("maestro", R.drawable.ic_card_branc_maestro),
  DINERS("diners", R.drawable.ic_card_brand_diners_club),
  DISCOVER("discover", R.drawable.ic_card_brand_discover),

  UNKNOWN("unknown", R.drawable.ic_credit_card);

  companion object {
    fun getPayment(brandName: String?): PaymentBrands {
      if (brandName.isNullOrEmpty()) return UNKNOWN
      return values().find { it.brandName == brandName } ?: UNKNOWN
    }
  }
}
