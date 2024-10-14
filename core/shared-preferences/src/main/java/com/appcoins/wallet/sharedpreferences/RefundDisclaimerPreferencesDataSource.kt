package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class RefundDisclaimerPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun setRefundDisclaimer(showRefundDisclaimer: Boolean) =
    sharedPreferences.edit().putBoolean(SHOW_REFUND_DISCLAIMER, showRefundDisclaimer).apply()

  fun getRefundDisclaimer() = sharedPreferences.getBoolean(SHOW_REFUND_DISCLAIMER, false)

  companion object {
    private const val SHOW_REFUND_DISCLAIMER = "show_refund_disclaimer"
  }
}