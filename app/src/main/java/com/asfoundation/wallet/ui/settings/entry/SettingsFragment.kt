package com.asfoundation.wallet.ui.settings.entry

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.asf.wallet.R
import com.asf.wallet.databinding.PreferenceFingerprintBinding
import com.asf.wallet.databinding.PreferenceFingerprintOffBinding
import com.asfoundation.wallet.billing.analytics.PageViewAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.change_currency.ChangeFiatCurrencyActivity
import com.asfoundation.wallet.change_currency.FiatCurrencyEntity
import com.asfoundation.wallet.change_currency.SettingsCurrencyPreference
import com.asfoundation.wallet.permissions.manage.view.ManagePermissionsActivity
import com.asfoundation.wallet.promo_code.SettingsPreferencePromoCodeState
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.subscriptions.SubscriptionActivity
import com.asfoundation.wallet.ui.settings.SettingsActivityView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), SettingsView {

  @Inject
  lateinit var pageViewAnalytics: PageViewAnalytics

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var presenter: SettingsPresenter
  private lateinit var activityView: SettingsActivityView
  private var switchSubject: PublishSubject<Unit>? = null

  companion object {

    const val TURN_ON_FINGERPRINT = "turn_on_fingerprint"

    @JvmStatic
    fun newInstance(turnOnFingerprint: Boolean = false): SettingsFragment {
      return SettingsFragment().apply {
        arguments = Bundle().apply {
          putBoolean(TURN_ON_FINGERPRINT, turnOnFingerprint)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is SettingsActivityView) {
      throw IllegalStateException("Settings Fragment must be attached to Settings Activity")
    }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    switchSubject = PublishSubject.create()
    presenter.setFingerPrintPreference()
  }

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.fragment_settings, rootKey)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
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

  override fun onDestroy() {
    switchSubject = null
    super.onDestroy()
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

  private fun openSubscriptionsScreen(): Boolean {
    context?.let {
      val intent = SubscriptionActivity.newIntent(it)
        .apply {
          flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
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

  override fun setCurrencyPreference(selectedCurrency: FiatCurrencyEntity) {
    val settingsCurrencyPreference = findPreference<SettingsCurrencyPreference>("pref_currency")
    settingsCurrencyPreference?.setCurrency(selectedCurrency)
    settingsCurrencyPreference?.setOnPreferenceClickListener {
      context?.let {
        val intent = ChangeFiatCurrencyActivity.newIntent(it)
          .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        startActivity(intent)
      }

      false
    }
  }

  override fun setBackupPreference() {
    val backupPreference = findPreference<Preference>("pref_backup")
    backupPreference?.setOnPreferenceClickListener {
      walletsEventSender.sendCreateBackupEvent(
        null,
        WalletsAnalytics.SETTINGS,
        null
      )
      presenter.onBackupPreferenceClick()
      false
    }
  }

  override fun setRestorePreference() {
    val restorePreference = findPreference<Preference>("pref_restore")
    restorePreference?.setOnPreferenceClickListener {
      presenter.onRecoverWalletPreferenceClick()
      false
    }
  }

  override fun setRedeemCodePreference(walletAddress: String) {
    val redeemPreference = findPreference<Preference>("pref_redeem")
    redeemPreference?.setOnPreferenceClickListener {
      presenter.onRedeemGiftPreferenceClick()
      false
    }
  }

  override fun setPromoCodePreference(promoCode: PromoCode) {
    val promoCodePreference = findPreference<SettingsPreferencePromoCodeState>("pref_promo_code")
    promoCodePreference?.setPromoCode(promoCode)
    promoCodePreference?.setOnPreferenceClickListener {
      presenter.onPromoCodePreferenceClick()
      false
    }
  }

  override fun navigateToIntent(intent: Intent) = startActivity(intent)

  override fun authenticationResult(): Observable<Boolean> {
    return activityView.authenticationResult()
  }

  override fun toggleFingerprint(enabled: Boolean) {
    setFingerprintPreference(enabled)
  }

  override fun setFingerprintPreference(hasAuthenticationPermission: Boolean) {
    val fingerprintPreference = findPreference<SwitchPreferenceCompat>("pref_fingerprint")

    if (hasAuthenticationPermission) {
      fingerprintPreference?.layoutResource = R.layout.preference_fingerprint
    } else {
      fingerprintPreference?.layoutResource = R.layout.preference_fingerprint_off
    }

    fingerprintPreference?.setOnPreferenceChangeListener { _, _ ->
      switchSubject?.onNext(Unit)
      true
    }
  }

  override fun updateFingerPrintListener(enabled: Boolean) {
    val fingerprintPreference = findPreference<SwitchPreferenceCompat>("pref_fingerprint")
    fingerprintPreference?.setOnPreferenceChangeListener { _, _ ->
      if (enabled) switchSubject?.onNext(Unit)
      true
    }
  }

  override fun switchPreferenceChange() = switchSubject!!

  override fun removeFingerprintPreference() {
    val fingerPrintPreference = findPreference<SwitchPreferenceCompat>("pref_fingerprint")
    fingerPrintPreference?.isVisible = false
  }

  override fun setDisabledFingerPrintPreference() {
    val fingerprintPreference = findPreference<SwitchPreferenceCompat>("pref_fingerprint")
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
      startBrowserActivity(Uri.parse("https://github.com/Catappult/appcoins-wallet-android"), false)
      false
    }
  }

  override fun setManageSubscriptionsPreference() {
    val subscriptionsPreference = findPreference<Preference>("pref_manage_subscriptions")
    subscriptionsPreference?.setOnPreferenceClickListener {
      openSubscriptionsScreen()
    }
  }

  override fun setIssueReportPreference() {
    val bugReportPreference = findPreference<Preference>("pref_contact_support")
    bugReportPreference?.setOnPreferenceClickListener {
      presenter.onBugReportClicked()
      false
    }
  }


  override fun setWithdrawPreference() {
    val bugReportPreference = findPreference<Preference>("pref_withdraw")
    bugReportPreference?.setOnPreferenceClickListener {
      presenter.onWithdrawClicked()
      false
    }

  }

  override fun setFaqsPreference() {
    val faqsPreference = findPreference<Preference>("pref_faqs")
    faqsPreference?.setOnPreferenceClickListener {
      startBrowserActivity(
        Uri.parse(
          "https://wallet.appcoins.io/faqs?lang=${
            Locale.getDefault().toLanguageTag()
          }"
        ),
        false
      )
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
      startBrowserActivity(
        Uri.parse(
          "https://wallet.appcoins.io/legal?section=privacy&lang=${
            Locale.getDefault().toLanguageTag()
          }"
        ),
        false
      )
      false
    }
  }

  override fun setTermsConditionsPreference() {
    val termsConditionsPreference = findPreference<Preference>("pref_terms_condition")
    termsConditionsPreference?.setOnPreferenceClickListener {
      startBrowserActivity(
        Uri.parse(
          "https://wallet.appcoins.io/legal?section=terms&lang=${
            Locale.getDefault().toLanguageTag()
          }"
        ),
        false
      )
      false
    }
  }

  override fun setCreditsPreference() {
    val creditsPreference = findPreference<Preference>("pref_credits")
    creditsPreference?.setOnPreferenceClickListener {
      AlertDialog.Builder(activity)
        .setPositiveButton(
          R.string.close
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