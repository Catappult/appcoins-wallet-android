package com.asfoundation.wallet.wallet_verification.intro

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.wallet_verification.code.WalletVerificationCodeFragment

class WalletVerificationIntroNavigator(private val fragmentManager: FragmentManager) {

  fun navigateToCodeView(currency: String, sign: String, value: String, digits: Int,
                         format: String, period: String, ts: Long) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            WalletVerificationCodeFragment.newInstance(currency, sign, value, digits, format,
                period, ts))
        .addToBackStack(WalletVerificationCodeFragment::class.java.simpleName)
        .commit()
  }

}
