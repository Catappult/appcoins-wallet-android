package com.asfoundation.wallet.ui.backup

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.entry.BackupEntryFragment
import javax.inject.Inject

class BackupActivityNavigator @Inject constructor(private val activity: Activity) {

  private val fragmentManager = (activity as AppCompatActivity).supportFragmentManager

  fun showBackupScreen(walletAddress: String) {
    fragmentManager.beginTransaction()
      .replace(R.id.fragment_container, BackupEntryFragment.newInstance(walletAddress))
      .commit()
  }
}
