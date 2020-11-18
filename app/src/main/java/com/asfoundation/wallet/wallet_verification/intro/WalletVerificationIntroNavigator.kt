package com.asfoundation.wallet.wallet_verification.intro

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.restore.password.RestoreWalletPasswordFragment
import com.asfoundation.wallet.wallet_verification.code.WalletVerificationCodeFragment

class WalletVerificationIntroNavigator(private val fragmentManager: FragmentManager) {

  fun navigateToCodeView(currency: String, value: String, digits: Int, format: String,
                         period: String, ts: Long) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            WalletVerificationCodeFragment.newInstance(currency, value, digits, format, period, ts))
        .addToBackStack(RestoreWalletPasswordFragment::class.java.simpleName)
        .commit()
  }

}
