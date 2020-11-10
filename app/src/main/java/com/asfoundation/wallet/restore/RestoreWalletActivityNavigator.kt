package com.asfoundation.wallet.restore

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.restore.intro.RestoreWalletFragment
import com.asfoundation.wallet.router.TransactionsRouter

class RestoreWalletActivityNavigator(private val context: Context,
                                     private val fragmentManager: FragmentManager) {

  fun navigateToTransactions() {
    TransactionsRouter().open(context, true)
  }

  fun navigateToInitialRestoreFragment() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            RestoreWalletFragment.newInstance())
        .commit()
  }
}
