package com.asfoundation.wallet.backup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.google.android.material.appbar.AppBarLayout
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
    if (!intent.getBooleanExtra(IS_BACKUP_TRIGGER, false)) {
      findViewById<AppBarLayout>(R.id.backup_wallet_app_bar).visibility = View.VISIBLE
      toolbar()
    }
    intent.getStringExtra(WALLET_ADDRESS_KEY)?.let { navigator.showBackupScreen(it) }

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