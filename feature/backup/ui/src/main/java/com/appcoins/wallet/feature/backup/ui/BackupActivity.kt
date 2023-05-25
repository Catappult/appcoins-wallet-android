package com.appcoins.wallet.feature.backup.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.wallet.appcoins.core.legacy_base.BaseActivity
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
    if (savedInstanceState == null) {
      if (!intent.getBooleanExtra(IS_BACKUP_TRIGGER, false)) {
        findViewById<AppBarLayout>(R.id.backup_wallet_app_bar).visibility = View.VISIBLE
        toolbar()
      }
      intent.getStringExtra(WALLET_ADDRESS_KEY)?.let { navigator.showBackupScreen(it) }
    }
  }

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
  fun toolbar(): Toolbar {
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar!!.visibility = View.VISIBLE
    if (toolbar != null) {
      setSupportActionBar(toolbar)
      toolbar.title = title
    }
    enableDisplayHomeAsUp()
    return toolbar
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