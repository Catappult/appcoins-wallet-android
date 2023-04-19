package com.asfoundation.wallet.permissions.request.view

import android.content.Context
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.permissions.PermissionName
import com.asf.wallet.R
import com.asfoundation.wallet.permissions.PermissionsInteractor
import com.appcoins.wallet.core.utils.android_common.applicationinfo.ApplicationInfoModel
import com.appcoins.wallet.core.utils.android_common.applicationinfo.ApplicationInfoProvider
import com.asf.wallet.databinding.FragmentPermissionsLayoutBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class PermissionFragment : BasePageViewFragment(), PermissionFragmentView {
  companion object {
    private const val CALLING_PACKAGE = "calling_package_key"
    private const val PERMISSION_KEY = "permission_key"
    private const val APK_SIGNATURE_KEY = "apk_signature_key"

    fun newInstance(callingPackage: String, apkSignature: String,
                    permission: PermissionName): PermissionFragment {

      return PermissionFragment().apply {
        arguments = Bundle().apply {
          putString(CALLING_PACKAGE, callingPackage)
          putString(APK_SIGNATURE_KEY, apkSignature)
          putSerializable(PERMISSION_KEY, permission)
        }
      }
    }
  }

  private lateinit var appInfoProvider: ApplicationInfoProvider

  @Inject
  lateinit var permissionsInteractor: PermissionsInteractor
  private lateinit var navigator: PermissionFragmentNavigator
  private lateinit var presenter: PermissionsFragmentPresenter
  private var disposable: Disposable? = null

  private val views by viewBinding(FragmentPermissionsLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val permission: PermissionName = arguments?.getSerializable(PERMISSION_KEY) as PermissionName
    presenter = PermissionsFragmentPresenter(this, permissionsInteractor,
        arguments?.getString(CALLING_PACKAGE)!!, permission,
        arguments?.getString(APK_SIGNATURE_KEY)!!, CompositeDisposable(),
        AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentPermissionsLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.mainView.visibility = View.INVISIBLE
    presenter.present()
  }

  override fun showAppData(packageName: String) {
    disposable?.dispose()
    disposable = Single.zip(Single.timer(500, TimeUnit.MILLISECONDS),
        Single.fromCallable { appInfoProvider.getAppInfo(packageName) },
        BiFunction { _: Long, app: ApplicationInfoModel -> app })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { app ->
          views.provideWalletAlwaysAllowWalletAppsLayout.provideWalletAlwaysAllowAppIcon.setImageDrawable(app.icon)

          val message = getString(R.string.provide_wallet_body, app.appName)
          val spannedMessage = SpannableString(message)
          val walletAppName = "AppCoins Wallet"

          if (message.indexOf(walletAppName) > -1) {
            spannedMessage.setSpan(StyleSpan(BOLD), message.indexOf(walletAppName),
                message.indexOf(walletAppName) + walletAppName.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          }
          spannedMessage.setSpan(StyleSpan(BOLD), message.indexOf(app.appName),
              message.indexOf(app.appName) + app.appName.length,
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
          views.provideWalletAlwaysAllowBody.text = spannedMessage
          views.progress.visibility = View.GONE
          views.mainView.visibility = View.VISIBLE
        }
        .subscribe()
  }

  override fun showWalletAddress(wallet: String) {
    views.provideWalletAlwaysAllowAppWalletAddress.text = wallet
  }

  override fun getAllowButtonClick(): Observable<Any> {
    return RxView.clicks(views.provideWalletAlwaysAllowButton)
  }

  override fun getAllowOnceClick(): Observable<Any> {
    return RxView.clicks(views.provideWalletAllowOnceButton)
  }

  override fun getCancelClick(): Observable<Any> {
    return RxView.clicks(views.provideWalletCancel)
  }

  override fun closeCancel() {
    navigator.closeCancel()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    when (context) {
      is PermissionFragmentNavigator -> {
        navigator = context
        appInfoProvider = ApplicationInfoProvider(context)
      }
      else -> throw IllegalArgumentException(
          "${PermissionFragment::class} has to be attached to an activity that implements ${PermissionFragmentNavigator::class}")
    }
  }

  override fun closeSuccess(walletAddress: String) {
    navigator.closeSuccess(walletAddress)
  }

  override fun onDestroyView() {
    presenter.stop()
    disposable?.dispose()
    super.onDestroyView()
  }
}
