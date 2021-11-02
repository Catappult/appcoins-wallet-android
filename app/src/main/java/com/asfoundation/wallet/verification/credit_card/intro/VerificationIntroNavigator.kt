package com.asfoundation.wallet.verification.credit_card.intro

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.verification.credit_card.code.VerificationCodeFragment

class VerificationIntroNavigator(private val fragmentManager: FragmentManager) {

  fun navigateToCodeView(currency: String, symbol: String, value: String, digits: Int,
                         format: String, period: String, ts: Long) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            VerificationCodeFragment.newInstance(currency, symbol, value, digits, format, period,
                ts))
        .addToBackStack(null)
        .commit()
  }
}
