package com.asfoundation.wallet.ui.backup

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.entry.BackupWalletFragment

class BackupActivityNavigator(private val fragmentManager: FragmentManager,
                              private val activity: FragmentActivity) {

  fun showBackupScreen(walletAddress: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupWalletFragment.newInstance(walletAddress))
        .commit()
  }
}
