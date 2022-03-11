package com.asfoundation.wallet.backup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupActivity : BaseActivity() {

  @Inject
  lateinit var navigator: BackupActivityNavigator

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"

    @JvmStatic
    fun newIntent(context: Context, walletAddress: String) =
      Intent(context, BackupActivity::class.java).apply {
        putExtra(WALLET_ADDRESS_KEY, walletAddress)
      }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_backup)

    toolbar()
    this.intent.getStringExtra(WALLET_ADDRESS_KEY)?.let { navigator.showBackupScreen(it) }
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