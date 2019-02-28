package com.asfoundation.wallet.permissions.request.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.appcoins.wallet.permissions.PermissionName
import com.asf.wallet.R
import com.asfoundation.wallet.permissions.PermissionsInteractor
import com.asfoundation.wallet.ui.BaseActivity
import com.jakewharton.rxrelay2.BehaviorRelay
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class PermissionsActivity : BaseActivity(), PermissionsActivityView, PermissionFragmentNavigator,
    CreateWalletNavigator {

  companion object {
    private const val PERMISSION_NAME_KEY = "PERMISSION_NAME_KEY"
  }

  @Inject
  lateinit var permissionsInteractor: PermissionsInteractor
  private lateinit var createWalletCompleteEvent: BehaviorRelay<Any>
  private var presenter: PermissionsActivityPresenter? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_permissions_layout)
    AndroidInjection.inject(this)

    createWalletCompleteEvent = BehaviorRelay.create()
    try {
      val permissionName = getPermission()
      presenter =
          PermissionsActivityPresenter(this, permissionsInteractor, callingPackage,
              getSignature(callingPackage), permissionName, CompositeDisposable(),
              AndroidSchedulers.mainThread())
      presenter?.present(savedInstanceState == null)
    } catch (e: IllegalArgumentException) {
      closeError(
          "Unknown permission name. \nKnown permissions: " + PermissionName.WALLET_ADDRESS.name)
    }
  }

  override fun getWalletCreatedEvent(): Observable<Any> {
    return createWalletCompleteEvent
  }

  override fun onDestroy() {
    presenter?.stop()
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

  private fun closeError(message: String) {
    val intent = Intent()
    intent.putExtra("ERROR_MESSAGE", message)
    setResult(Activity.RESULT_CANCELED, intent)
    finish()
  }

  @Throws(IllegalArgumentException::class)
  private fun getPermission(): PermissionName {
    return PermissionName.valueOf(intent.extras[PERMISSION_NAME_KEY] as String)
  }

  override fun showPermissionFragment(callingPackage: String,
                                      permission: PermissionName, apkSignature: String) {
    showFragment(PermissionFragment.newInstance(callingPackage, apkSignature, permission))
  }

  override fun showWalletCreation() {
    showFragment(CreateWalletFragment.newInstance())
  }

  private fun showFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
  }

  override fun closeSuccess() {
    createWalletCompleteEvent.accept(Any())
  }

}