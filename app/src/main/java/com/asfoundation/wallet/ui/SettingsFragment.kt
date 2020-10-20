package com.asfoundation.wallet.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.PageViewAnalytics
import com.asfoundation.wallet.permissions.manage.view.ManagePermissionsActivity
import com.asfoundation.wallet.ui.balance.RestoreWalletActivity
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.preference_fingerprint.*
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), SettingsView {

  @Inject
  lateinit var settingsInteract: SettingsInteract

  @Inject
  lateinit var pageViewAnalytics: PageViewAnalytics

  private lateinit var presenter: SettingsPresenter
  private lateinit var activityView: SettingsActivityView

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is SettingsActivityView) {
      throw IllegalStateException("Settings Fragment must be attached to Settings Activity")
    }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidSupportInjection.inject(this)
    super.onCreate(savedInstanceState)
    presenter =
        SettingsPresenter(this, activityView, Schedulers.io(), AndroidSchedulers.mainThread(),
            CompositeDisposable(), settingsInteract)
    presenter.present()
  }

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.fragment_settings, rootKey)
  }

  override fun onResume() {
    super.onResume()
    pageViewAnalytics.sendPageViewEvent(javaClass.simpleName)
    presenter.onResume()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private fun startBrowserActivity(uri: Uri, newTaskFlag: Boolean) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, uri)
      if (newTaskFlag) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      showError()
    }
  }

  private fun openPermissionScreen(): Boolean {
    context?.let {
      val intent = ManagePermissionsActivity.newIntent(it)
          .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
      startActivity(intent)
    }
    return true
  }

  override fun showError() {
    view?.let {
      Snackbar.make(it, R.string.unknown_error, Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  override fun setBackupPreference() {
    val backupPreference = findPreference<Preference>("pref_backup")
    backupPreference?.setOnPreferenceClickListener {
      presenter.onBackupPreferenceClick()
      false
    }
  }

  override fun setRestorePreference() {
    val restorePreference = findPreference<Preference>("pref_restore")
    restorePreference?.setOnPreferenceClickListener {
      context?.let { startActivity(RestoreWalletActivity.newIntent(it)) }
      false
    }
  }

  override fun setRedeemCodePreference(walletAddress: String) {
    val redeemPreference = findPreference<Preference>("pref_redeem")
    redeemPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse(
          BuildConfig.MY_APPCOINS_BASE_HOST + "redeem?wallet_address=" + walletAddress),
          false)
      false
    }
  }

  override fun navigateToIntent(intent: Intent) = startActivity(intent)

  override fun authenticationResult(): Observable<Boolean> {
    return activityView.authenticationResult()
  }

  override fun toggleFingerprint(enabled: Boolean) {
    pref_authentication_switch.isChecked = enabled
  }

  override fun setFingerprintPreference(hasAuthenticationPermission: Boolean) {
    val fingerprintPreference = findPreference<SwitchPreference>("pref_fingerprint")
    fingerprintPreference?.isChecked = hasAuthenticationPermission

    if (hasAuthenticationPermission) {
      fingerprintPreference?.layoutResource = R.layout.preference_fingerprint
    } else {
      fingerprintPreference?.layoutResource = R.layout.preference_fingerprint_off
    }

    fingerprintPreference?.setOnPreferenceChangeListener { _, newValue ->
      presenter.onFingerPrintPreferenceChange(newValue as Boolean)
      true
    }
  }

  override fun removeFingerprintPreference() {
    val fingerPrintPreference = findPreference<SwitchPreference>("pref_fingerprint")
    fingerPrintPreference?.isVisible = false
  }

  override fun setDisabledFingerPrintPreference() {
    val fingerprintPreference = findPreference<SwitchPreference>("pref_fingerprint")
    fingerprintPreference?.isChecked = false
    fingerprintPreference?.layoutResource = R.layout.preference_fingerprint_off
    fingerprintPreference?.setOnPreferenceChangeListener { _, _ ->
      true
    }
  }

  override fun setPermissionPreference() {
    val permissionPreference = findPreference<Preference>("pref_permissions")
    permissionPreference?.setOnPreferenceClickListener { openPermissionScreen() }
  }

  override fun setSourceCodePreference() {
    val sourceCodePreference = findPreference<Preference>("pref_source_code")
    sourceCodePreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://github.com/Aptoide/appcoins-wallet-android"), false)
      false
    }
  }

  override fun setIssueReportPreference() {
    val bugReportPreference = findPreference<Preference>("pref_contact_support")
    bugReportPreference?.setOnPreferenceClickListener {
      presenter.onBugReportClicked()
      false
    }
  }

  override fun setTwitterPreference() {
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
  }

  override fun setTelegramPreference() {
    val telegramPreference = findPreference<Preference>("pref_telegram")
    telegramPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://t.me/appcoinsofficial"), false)
      false
    }
  }

  override fun setFacebookPreference() {
    val facebookPreference = findPreference<Preference>("pref_facebook")
    facebookPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://www.facebook.com/AppCoinsOfficial"), false)
      false
    }
  }

  override fun setEmailPreference() {
    val emailPreference = findPreference<Preference>("pref_email")
    emailPreference?.setOnPreferenceClickListener {
      val email = "info@appcoins.io"
      val subject = "Android wallet support question"
      val body = "Dear AppCoins support,"
      val emailAppIntent = Intent(Intent.ACTION_SENDTO)
      emailAppIntent.data = Uri.parse("mailto:")
      emailAppIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
      emailAppIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
      emailAppIntent.putExtra(Intent.EXTRA_TEXT, body)
      startActivity(Intent.createChooser(emailAppIntent, "Select email application."))
      true
    }
  }

  override fun setPrivacyPolicyPreference() {
    val privacyPolicyPreference = findPreference<Preference>("pref_privacy_policy")
    privacyPolicyPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://catappult.io/appcoins-wallet/privacy-policy"),
          false)
      false
    }
  }

  override fun setTermsConditionsPreference() {
    val termsConditionsPreference = findPreference<Preference>("pref_terms_condition")
    termsConditionsPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://catappult.io/appcoins-wallet/terms-conditions"),
          false)
      false
    }
  }

  override fun setCreditsPreference() {
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
  }

  override fun setVersionPreference() {
    val versionString = getVersion()
    val versionPreference = findPreference<Preference>("pref_version")
    versionPreference?.summary = getString(R.string.check_updates_settings_subtitle, versionString)
    versionPreference?.setOnPreferenceClickListener {
      presenter.redirectToStore()
      false
    }
  }

  private fun getVersion(): String? {
    var version: String? = "N/A"
    try {
      activity?.let {
        val pInfo = it.packageManager?.getPackageInfo(it.packageName, 0)
        version = pInfo?.versionName
      }
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    return version
  }
}