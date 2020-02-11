package com.asfoundation.wallet.billing.adyen

import androidx.annotation.StringRes
import com.asf.wallet.R

class AdyenErrorCodeMapper {

  @StringRes
  internal fun map(errorCode: Int): Int {
    return when (errorCode) {
      2, 5, 22, 23, 26, 27, 31 -> R.string.purchase_card_error_general_2
      3, 4, 9 -> R.string.purchase_card_error_general_1
      6 -> R.string.purchase_card_error_expired
      7, 12, 25 -> R.string.purchase_card_error_no_funds
      8 -> R.string.purchase_card_error_invalid_details
      10 -> R.string.purchase_card_error_not_supported
      17, 18 -> R.string.purchase_card_error_security
      20 -> R.string.NA
      else -> R.string.purchase_card_error_invalid_details
    }
  }

}
