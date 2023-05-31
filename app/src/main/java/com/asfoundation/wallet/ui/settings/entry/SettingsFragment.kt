package com.asfoundation.wallet.ui.settings.entry

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrency
import com.appcoins.wallet.ui.widgets.TopBar
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.PageViewAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.change_currency.ChangeFiatCurrencyFragment
import com.asfoundation.wallet.change_currency.SettingsCurrencyPreference
import com.asfoundation.wallet.permissions.manage.view.ManagePermissionsActivity
import com.asfoundation.wallet.subscriptions.SubscriptionActivity
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.settings.SettingsActivityView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), SettingsView {

  @Inject
  lateinit var pageViewAnalytics: PageViewAnalytics

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var presenter: SettingsPresenter
  private var switchSubject: PublishSubject<Unit>? = null
  private lateinit var authenticationResultLauncher: ActivityResultLauncher<Intent>


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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    switchSubject = PublishSubject.create()
    presenter.setFingerPrintPreference()
    handleAuthenticationResult()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
    view.findViewById<ComposeView>(R.id.app_bar).apply {
      setContent {
        TopBar(isMainBar = false, onClickSupport = { presenter.displayChat() })
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val layout = inflater.inflate(R.layout.fragment_settings, container, false)
    val settingsContainer = layout.findViewById<FrameLayout>(R.id.settings_container_view)
    val settingsView = super.onCreateView(inflater, settingsContainer, savedInstanceState)
    settingsContainer.addView(settingsView)
    return layout
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
    authenticationResultLauncher.unregister()
    super.onDestroy()
  }

  private fun handleAuthenticationResult() {
    authenticationResultLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AuthenticationPromptActivity.RESULT_OK) {
          val hasPermission = presenter.hasAuthenticationPermission()
          presenter.changeAuthorizationPermission()
          toggleFingerprint(!hasPermission)
        } else {
          Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
        }
      }
  }

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.settings_options, rootKey)
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

  override fun setCurrencyPreference(selectedCurrency: FiatCurrency) {
    val settingsCurrencyPreference = findPreference<SettingsCurrencyPreference>("pref_currency")
    settingsCurrencyPreference?.setCurrency(selectedCurrency)
    settingsCurrencyPreference?.setOnPreferenceClickListener {
      parentFragmentManager
        .beginTransaction()
        .replace(
          R.id.container,
          ChangeFiatCurrencyFragment.newInstance()
        ).commit()
      false
    }
  }

  override fun setManageWalletPreference() {
    val manageWalletPreference = findPreference<Preference>("pref_manage_wallet")
    manageWalletPreference?.setOnPreferenceClickListener {
      presenter.onManageWalletPreferenceClick(navController())
      false
    }
  }

  override fun setAccountPreference() {
    val accountPreference = findPreference<Preference>("pref_account")
    accountPreference?.setOnPreferenceClickListener {
      Toast.makeText(context, "In Progress", Toast.LENGTH_SHORT)
        .show() // TODO create account screen
      false
    }
  }

  override fun navigateToIntent(intent: Intent) = startActivity(intent)

  override fun authenticationResult(): ActivityResultLauncher<Intent> = authenticationResultLauncher

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

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }
}