package com.asfoundation.wallet.ui.backup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ActivityCompat
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
    private const val RC_WRITE_EXTERNAL_STORAGE_PERMISSION = 1000
  }

  @Inject
  lateinit var presenter: BackupActivityPresenter
  private var onPermissionSubject: PublishSubject<Unit>? = null
  private var onDocumentFileSubject: PublishSubject<SystemFileIntentResult>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_backup)
    onPermissionSubject = PublishSubject.create()
    onDocumentFileSubject = PublishSubject.create()
    presenter.present(savedInstanceState == null)
  }

  override fun askForWritePermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
        ActivityCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
      onPermissionSubject?.onNext(Unit)
    } else {
      requestStorageWritePermission()
    }
  }

  override fun closeScreen() = finish()

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> presenter.sendWalletSaveFileEvent()
    }
    return super.onOptionsItemSelected(item)
  }

  private fun requestStorageWritePermission() {
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        RC_WRITE_EXTERNAL_STORAGE_PERMISSION)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                          grantResults: IntArray) {
    if (requestCode == RC_WRITE_EXTERNAL_STORAGE_PERMISSION) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        onPermissionSubject?.onNext(Unit)
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.onActivityResult(requestCode, resultCode, data, this)
  }

  override fun onDocumentFile(systemFileIntentResult: SystemFileIntentResult) {
    onDocumentFileSubject?.onNext(systemFileIntentResult)
  }

  override fun onSystemFileIntentResult() = onDocumentFileSubject!!

  override fun onPermissionGiven() = onPermissionSubject!!

  override fun getCurrentFragment(): String {
    val fragments = supportFragmentManager.fragments
    return if (fragments.isNotEmpty()) fragments[0]::class.java.simpleName
    else BackupWalletFragment::class.java.simpleName
  }

  override fun setupToolbar() {
    toolbar()
  }

  override fun onDestroy() {
    onPermissionSubject = null
    onDocumentFileSubject = null
    super.onDestroy()
  }
}