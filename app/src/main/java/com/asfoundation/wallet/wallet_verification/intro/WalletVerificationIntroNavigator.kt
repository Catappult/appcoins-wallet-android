package com.asfoundation.wallet.wallet_verification.intro

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.ActivityNavigatorContract
import com.asfoundation.wallet.restore.password.RestoreWalletPasswordFragment
import com.asfoundation.wallet.wallet_verification.code.WalletVerificationCodeFragment

class WalletVerificationIntroNavigator(private val fragmentManager: FragmentManager,
                                       private val activityNavigator: ActivityNavigatorContract) {

  fun navigateToCodeView() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, WalletVerificationCodeFragment.newInstance())
        .addToBackStack(RestoreWalletPasswordFragment::class.java.simpleName)
        .commit()
  }
}
