package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.backup.entry.BackupWalletFragment
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class BackupActivity : BaseActivity(), BackupActivityView, ToolbarManager {

  companion object {
    @JvmStatic
    fun newIntent(context: Context, walletAddress: String) =
        Intent(context, BackupActivity::class.java).apply {
          putExtra(WALLET_ADDRESS, walletAddress)
        }

    const val WALLET_ADDRESS = "wallet_addr"
    const val ACTION_OPEN_DOCUMENT_TREE_REQUEST_CODE = 1001
  }

  @Inject
  lateinit var presenter: BackupActivityPresenter
  private lateinit var onDocumentFileSubject: PublishSubject<SystemFileIntentResult>

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_backup)
    onDocumentFileSubject = PublishSubject.create()
    presenter.present(savedInstanceState == null)
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

  override fun closeScreen() = finish()

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.onActivityResult(requestCode, resultCode, data, this)
  }

  override fun onDocumentFile(systemFileIntentResult: SystemFileIntentResult) {
    onDocumentFileSubject.onNext(systemFileIntentResult)
  }

  override fun onSystemFileIntentResult() = onDocumentFileSubject

  override fun setupToolbar() {
    toolbar()
  }

  private fun getCurrentFragment(): String {
    val fragments = supportFragmentManager.fragments
    return if (fragments.isNotEmpty()) fragments[0]::class.java.simpleName
    else BackupWalletFragment::class.java.simpleName
  }


}