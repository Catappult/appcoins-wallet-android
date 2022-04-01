package com.asfoundation.wallet.backup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.util.safeLet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupActivity : BaseActivity() {

  @Inject
  lateinit var navigator: BackupActivityNavigator

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val IS_BACKUP_TRIGGER = "is_backup_trigger"

    @JvmStatic
    fun newIntent(context: Context, walletAddress: String, isBackupTrigger: Boolean) =
      Intent(context, BackupActivity::class.java).apply {
        putExtra(WALLET_ADDRESS_KEY, walletAddress)
        putExtra(IS_BACKUP_TRIGGER, isBackupTrigger)
      }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_backup)

    toolbar()
    safeLet(
      intent.getStringExtra(WALLET_ADDRESS_KEY),
      intent.getBooleanExtra(IS_BACKUP_TRIGGER, false)
    ) { walletAddress, isBackupTrigger ->
      navigator.showBackupScreen(walletAddress, isBackupTrigger)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      if (supportFragmentManager.backStackEntryCount >= 1) {
        supportFragmentManager.popBackStack()
      } else {
        finish()
      }
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}