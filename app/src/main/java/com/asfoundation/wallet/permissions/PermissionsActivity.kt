package com.asfoundation.wallet.permissions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.appcoins.wallet.permissions.PermissionName
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class PermissionsActivity : BaseActivity(), PermissionsActivityView, PermissionFragmentNavigator {

  companion object {
    private const val PERMISSION_NAME_KEY = "PERMISSION_NAME_KEY"
  }

  @Inject
  lateinit var permissionsInteractor: PermissionsInteractor
  private lateinit var presenter: PermissionsActivityPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_permissions_layout)
    AndroidInjection.inject(this)
    presenter =
        PermissionsActivityPresenter(this, permissionsInteractor, callingPackage,
            getSignature(callingPackage), getPermission(), CompositeDisposable())
    presenter.present(savedInstanceState == null)
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  private fun getSignature(callingPackage: String): String {
    val signature = StringBuilder()
    for (sig in packageManager
        .getPackageInfo(callingPackage, PackageManager.GET_SIGNATURES).signatures) {
      signature.append(String(sig.toByteArray()))
    }
    return signature.toString()
  }

  override fun closeSuccess(walletAddress: String) {
    val intent = Intent()
    intent.putExtra("WALLET_ADDRESS", walletAddress)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun closeCancel() {
    val intent = Intent()
    setResult(Activity.RESULT_CANCELED, intent)
    finish()
  }

  private fun getPermission(): PermissionName {
    return PermissionName.valueOf(intent.extras[PERMISSION_NAME_KEY] as String)
  }

  override fun showPermissionFragment(callingPackage: String,
                                      permission: PermissionName, apkSignature: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PermissionFragment.newInstance(callingPackage, apkSignature, permission))
        .commit()
  }

}