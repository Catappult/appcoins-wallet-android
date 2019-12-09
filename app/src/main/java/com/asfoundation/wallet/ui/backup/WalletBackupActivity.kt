package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.topup.TopUpFragment
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection

class WalletBackupActivity : BaseActivity(), BackupActivityView, ToolbarManager {

  companion object {
    @JvmStatic
    fun newIntent(context: Context, walletAddress: String): Intent {
      val intent = Intent(context, WalletBackupActivity::class.java)
      intent.putExtra(WALLET_ADDRESS, walletAddress)
      return intent
    }

    private const val WALLET_ADDRESS = "wallet_addr"

  }
  private lateinit var presenter: BackupActivityPresenter

  val walletAddr: String by lazy {
    if (intent.extras!!.containsKey(WALLET_ADDRESS)) {
      intent.extras!!.getString(WALLET_ADDRESS)
    } else {
      throw IllegalArgumentException("application package name data not found")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_backup)
    presenter = BackupActivityPresenter(this)
    presenter.present(savedInstanceState == null)
  }

  override fun showBackupScreen() {
    setupToolbar()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupWalletFragment.newInstance(walletAddr))
        .commit()
  }

  override fun showConfirmationScreen() {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun setupToolbar() {
    toolbar()
  }
}