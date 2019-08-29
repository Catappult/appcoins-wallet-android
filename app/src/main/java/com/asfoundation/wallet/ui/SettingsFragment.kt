package com.asfoundation.wallet.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.permissions.manage.view.ManagePermissionsActivity
import com.asfoundation.wallet.router.ManageWalletsRouter
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationActivity
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SettingsFragment(private var networkScheduler: Scheduler,
                       private var viewScheduler: Scheduler) : PreferenceFragmentCompat() {

  @Inject
  internal lateinit var findDefaultWalletInteract: FindDefaultWalletInteract
  @Inject
  internal lateinit var manageWalletsRouter: ManageWalletsRouter
  @Inject
  lateinit var smsValidationInteract: SmsValidationInteract

  private lateinit var disposables: CompositeDisposable

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidSupportInjection.inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.fragment_settings, rootKey)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    disposables = CompositeDisposable()

    setWalletsPreference()

    setWalletValidationPreference()

    val permissionPreference = findPreference<Preference>("pref_permissions")
    permissionPreference?.setOnPreferenceClickListener {
      openPermissionScreen()
    }

    val redeemPreference = findPreference<Preference>("pref_redeem")
    redeemPreference?.setOnPreferenceClickListener {
      findDefaultWalletInteract.find()
          .subscribe { wallet ->
            startBrowserActivity(Uri.parse(
                BuildConfig.MY_APPCOINS_BASE_HOST + "redeem?wallet_address=" + wallet.address),
                false)
          }
      false
    }

    val sourceCodePreference = findPreference<Preference>("pref_source_code")
    sourceCodePreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://github.com/Aptoide/appcoins-wallet-android"), false)
      false
    }

    val bugReportPreference = findPreference<Preference>("pref_report_bug")
    bugReportPreference?.setOnPreferenceClickListener {
      startBrowserActivity(
          Uri.parse("https://github.com/Aptoide/appcoins-wallet-android/issues/new"), false)
      false
    }

    val twitterPreference = findPreference<Preference>("pref_twitter")
    twitterPreference?.setOnPreferenceClickListener {
      try {
        activity?.packageManager?.getPackageInfo("com.twitter.android", 0)
        startBrowserActivity(Uri.parse("twitter://user?user_id=915531221551255552"), true)
      } catch (e: Exception) {
        startBrowserActivity(Uri.parse("https://twitter.com/AppCoinsProject"), false)
      }

      false
    }

    val facebookPreference = findPreference<Preference>("pref_facebook")
    facebookPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://www.facebook.com/AppCoinsOfficial"), false)
      false
    }

    val telegramPreference = findPreference<Preference>("pref_telegram")
    telegramPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://t.me/appcoinsofficial"), false)
      false
    }

    val emailPreference = findPreference<Preference>("pref_email")
    emailPreference?.setOnPreferenceClickListener {
      val mailto = Intent(Intent.ACTION_SEND_MULTIPLE)
      mailto.type = "message/rfc822" // use from live device
      mailto.putExtra(Intent.EXTRA_EMAIL, arrayOf("info@appcoins.io"))
      mailto.putExtra(Intent.EXTRA_SUBJECT, "Android wallet support question")
      mailto.putExtra(Intent.EXTRA_TEXT, "Dear AppCoins support,")

      startActivity(Intent.createChooser(mailto, "Select email application."))
      true
    }

    val privacyPolicyPreference = findPreference<Preference>("pref_privacy_policy")
    privacyPolicyPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://catappult.io/appcoins-wallet/privacy-policy"), false)
      false
    }

    val termsConditionsPreference = findPreference<Preference>("pref_terms_condition")
    termsConditionsPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://catappult.io/appcoins-wallet/terms-conditions"),
          false)
      false
    }

    val creditsPreference = findPreference<Preference>("pref_credits")
    creditsPreference?.setOnPreferenceClickListener {
      AlertDialog.Builder(activity)
          .setPositiveButton(R.string.close
          ) { dialog, _ -> dialog.dismiss() }
          .setMessage(R.string.settings_fragment_credits)
          .create()
          .show()
      true
    }

    val versionString = getVersion()
    val versionPreference = findPreference<Preference>("pref_version")
    versionPreference?.summary = versionString
  }

  override fun onResume() {
    setWalletsPreference()
    setWalletValidationPreference()
    super.onResume()
  }

  override fun onDestroyView() {
    disposables.dispose()
    super.onDestroyView()
  }

  private fun startBrowserActivity(uri: Uri, newTaskFlag: Boolean) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, uri)
      if (newTaskFlag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      if (view != null) {
        view?.let {
          Snackbar.make(it, R.string.unknown_error, Snackbar.LENGTH_SHORT)
              .show()
        }
      }
    }
  }

  private fun openPermissionScreen(): Boolean {
    context?.let { startActivity(ManagePermissionsActivity.newIntent(it)) }
    return true
  }

  private fun openWalletValidationScreen(): Boolean {
    context?.let {
      startActivity(WalletValidationActivity.newIntent(it,
          getString(R.string.verification_settings_unverified_title)))
    }
    return true
  }

  private fun setWalletValidationPreference() {
    val verifyWalletPreference = findPreference<Preference>("pref_verification")

    disposables.add(findDefaultWalletInteract.find()
        .flatMap { smsValidationInteract.isValid(Wallet(it.address)) }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it == WalletValidationStatus.SUCCESS) {
            verifyWalletPreference?.summary =
                getString(R.string.verification_settings_verified_title)
            verifyWalletPreference?.onPreferenceClickListener = null
          } else {
            verifyWalletPreference?.summary =
                getString(R.string.verification_settings_unverified_body)
            verifyWalletPreference?.setOnPreferenceClickListener { openWalletValidationScreen() }
          }
        }
        .subscribe())
  }

  private fun setWalletsPreference() {
    val walletPreference = findPreference<Preference>("pref_wallet")
    walletPreference?.setOnPreferenceClickListener {
      manageWalletsRouter.open(activity, false)
      false
    }

    disposables.add(findDefaultWalletInteract.find()
        .subscribe({ wallet ->
          PreferenceManager.getDefaultSharedPreferences(view?.context)
              .edit()
              .putString("pref_wallet", wallet.address)
              .apply()
          walletPreference?.summary = wallet.address
        }, {}))
  }

  private fun getVersion(): String? {
    var version: String? = "N/A"
    try {
      val pInfo = activity?.packageManager?.getPackageInfo(activity?.packageName, 0)
      version = pInfo?.versionName
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    return version
  }
}