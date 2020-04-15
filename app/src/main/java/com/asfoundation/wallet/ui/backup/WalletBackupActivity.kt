package com.asfoundation.wallet.ui.backup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.asf.wallet.R
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
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
    private const val RC_WRITE_EXTERNAL_STORAGE_PERMISSION = 1000

  }

  private lateinit var presenter: BackupActivityPresenter

  private val walletAddr: String by lazy {
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

  override fun showBackupCreationScreen() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupCreationFragment.newInstance())
        .commit()
  }

  override fun showConfirmationScreen() {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun startWalletBackup() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this, "backup wallet in file", Toast.LENGTH_LONG)
    } else {
      requestStorageWritePermission()
    }
  }

  private fun requestStorageWritePermission() {
    Log.e("TEST", "Requesting file system write permission")
    Log.e("TEST", "SHOW RATIONAL"
        + ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE))

    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        RC_WRITE_EXTERNAL_STORAGE_PERMISSION)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                          grantResults: IntArray) {
    if (requestCode == RC_WRITE_EXTERNAL_STORAGE_PERMISSION) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.e("TEST", "PERMISSION GRANTED")
      } else {
        Log.e("", "Permission not granted: results len = "
            + grantResults.size
            + " Result code = "
            + grantResults[0])
      }
    } else {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }
}

override fun setupToolbar() {
  toolbar()
}
}