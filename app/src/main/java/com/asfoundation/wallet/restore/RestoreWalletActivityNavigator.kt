package com.asfoundation.wallet.restore

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.main.MainActivityNavigator
import com.asfoundation.wallet.restore.intro.RestoreWalletFragment

class RestoreWalletActivityNavigator(private val mainActivityNavigator: MainActivityNavigator,
                                     private val fragmentManager: FragmentManager) {

  fun navigateToTransactions() {
    mainActivityNavigator.navigateToHome()
  }

  fun navigateToInitialRestoreFragment() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            RestoreWalletFragment.newInstance())
        .commit()
  }
}
