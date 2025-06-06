package com.asfoundation.wallet.ui.settings.entry

import android.app.Activity
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.PageViewAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.analytics.analytics.manage_cards.ManageCardsAnalytics
import com.appcoins.wallet.core.utils.properties.PRIVACY_POLICY_URL
import com.appcoins.wallet.core.utils.properties.TERMS_CONDITIONS_URL
import com.appcoins.wallet.core.utils.properties.UrlPropertiesFormatter
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrency
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.asf.wallet.R
import com.asfoundation.wallet.change_currency.SettingsCurrencyPreference
import com.asfoundation.wallet.manage_cards.ManageCardSharedViewModel
import com.asfoundation.wallet.permissions.manage.view.ManagePermissionsActivity
import com.asfoundation.wallet.subscriptions.SubscriptionActivity
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.webview_login.WebViewLoginActivity
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
  lateinit var analytics: SettingsAnalytics

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var manageCardsAnalytics: ManageCardsAnalytics

  @Inject
  lateinit var presenter: SettingsPresenter

  private var switchSubject: PublishSubject<Unit>? = null
  private lateinit var authenticationResultLauncher: ActivityResultLauncher<Intent>

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private val manageCardSharedViewModel: ManageCardSharedViewModel by activityViewModels()

  companion object {
    const val TURN_ON_FINGERPRINT = "turn_on_fingerprint"

    const val MANAGE_WALLET_EVENT = "manage_wallet"

    @JvmStatic
    fun newInstance(turnOnFingerprint: Boolean = false): SettingsFragment {
      return SettingsFragment().apply {
        arguments = Bundle().apply {
          putBoolean(TURN_ON_FINGERPRINT, turnOnFingerprint)
        }
      }
    }
  }

  private val openLoginLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      when (result.resultCode) {
        Activity.RESULT_OK -> {}

        Activity.RESULT_CANCELED -> {}

        else -> {}
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
        TopBar(
          isMainBar = false,
          onClickSupport = { presenter.displayChat() },
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics
        )
      }
    }
    if (manageCardSharedViewModel.isCardSaved.value) {
      manageCardsAnalytics.addedNewCardSuccessEvent()
      Toast.makeText(context, R.string.card_added_title, Toast.LENGTH_LONG)
        .show()
      manageCardSharedViewModel.resetCardResult()
    } else if (manageCardSharedViewModel.isCardError.value) {
      Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_LONG)
        .show()
      manageCardSharedViewModel.resetCardResult()
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

  override fun setCurrencyPreference(selectedCurrency: FiatCurrency?) {
    val settingsCurrencyPreference = findPreference<SettingsCurrencyPreference>("pref_currency")
    selectedCurrency?.let {
      settingsCurrencyPreference?.setCurrency(selectedCurrency)
    }
    settingsCurrencyPreference?.setOnPreferenceClickListener {
      presenter.onChangeCurrencyPreferenceClick(navController())
      false
    }
  }

  override fun setManageWalletPreference() {
    val manageWalletPreference = findPreference<Preference>("pref_manage_wallet")
    manageWalletPreference?.setOnPreferenceClickListener {
      analytics.sendManageWalletScreenEvent(action = MANAGE_WALLET_EVENT)
      presenter.onManageWalletPreferenceClick(navController())
      false
    }
  }

  override fun setLoginPreference() {
    val loginPreference = findPreference<Preference>("pref_login")
    loginPreference?.setOnPreferenceClickListener {
      val url = presenter.getLoginUrl()
      val intent = Intent(requireContext(), WebViewLoginActivity::class.java)
      intent.putExtra(WebViewLoginActivity.URL, url)
      openLoginLauncher.launch(intent)
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

  override fun setManageCardsPreference() {
    val manageCardsPreference = findPreference<Preference>("pref_manage_cards")
    manageCardsPreference?.layoutResource = R.layout.preference_without_summary_layout
    manageCardsPreference?.title = getString(R.string.manage_cards_settings_manage_title)
    manageCardsPreference?.setOnPreferenceClickListener {
      manageCardsAnalytics.settingsManageCardsClickEvent()
      presenter.onManageCardsPreferenceClick(navController())
      false
    }
  }

  override fun setSkeletonCardPreference() {
    val manageCardsPreference = findPreference<Preference>("pref_manage_cards")
    manageCardsPreference?.layoutResource = R.layout.skeleton_settings
    manageCardsPreference?.title = ""
  }

  override fun setAddNewCardPreference() {
    val addCardsPreference = findPreference<Preference>("pref_manage_cards")
    addCardsPreference?.layoutResource = R.layout.preference_without_summary_layout
    addCardsPreference?.title = getString(R.string.manage_cards_settings_add_title)
    addCardsPreference?.setOnPreferenceClickListener {
      manageCardsAnalytics.settingsManageCardsClickEvent()
      presenter.onAddCardsPreferenceClick(navController())
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

  override fun setDiscordPreference() {
    val discordPreference = findPreference<Preference>("pref_discord")
    discordPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://discord.gg/aptoide"), false)
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
        uri = UrlPropertiesFormatter.addLanguageElementToUrl(PRIVACY_POLICY_URL),
        newTaskFlag = false
      )
      false
    }
  }

  override fun setTermsConditionsPreference() {
    val termsConditionsPreference = findPreference<Preference>("pref_terms_condition")
    termsConditionsPreference?.setOnPreferenceClickListener {
      startBrowserActivity(
        uri = UrlPropertiesFormatter.addLanguageElementToUrl(TERMS_CONDITIONS_URL),
        newTaskFlag = false
      )
      false
    }
  }

  override fun setCreditsPreference() {
    val creditsPreference = findPreference<Preference>("pref_credits")
    creditsPreference?.setOnPreferenceClickListener {
      val bottomSheet = SettingsCreditsBottomSheetFragment.newInstance()
      bottomSheet.show(parentFragmentManager, "ManageWalletName")
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