package com.asfoundation.wallet.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.permissions.manage.view.ManagePermissionsActivity
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.subscriptions.SubscriptionActivity
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationActivity
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), SettingsView {

  @Inject
  internal lateinit var findDefaultWalletInteract: FindDefaultWalletInteract

  @Inject
  lateinit var smsValidationInteract: SmsValidationInteract

  @Inject
  lateinit var preferencesRepositoryType: PreferencesRepositoryType

  @Inject
  lateinit var supportInteractor: SupportInteractor
  private lateinit var presenter: SettingsPresenter


  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidSupportInjection.inject(this)
    super.onCreate(savedInstanceState)
    presenter = SettingsPresenter(this, Schedulers.io(), AndroidSchedulers.mainThread(),
        CompositeDisposable(), findDefaultWalletInteract, smsValidationInteract,
        preferencesRepositoryType)
  }

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.fragment_settings, rootKey)
  }

  override fun onResume() {
    super.onResume()
    presenter.present()
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
      view?.let {
        Snackbar.make(it, R.string.unknown_error, Snackbar.LENGTH_SHORT)
            .show()
      }
    }
  }

  private fun openPermissionScreen(): Boolean {
    context?.let {
      val intent = ManagePermissionsActivity.newIntent(it)
          .apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
          }
      startActivity(intent)
    }
    return true
  }

  private fun openSubscriptionsScreen(): Boolean {
    context?.let {
      val intent = SubscriptionActivity.newIntent(it, SubscriptionActivity.ACTION_LIST)
          .apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
          }
      startActivity(intent)
    }
    return true
  }

  private fun openWalletValidationScreen(): Boolean {
    context?.let {
      val intent = WalletValidationActivity.newIntent(it, hasBeenInvitedFlow = false,
          navigateToTransactionsOnSuccess = true, navigateToTransactionsOnCancel = false,
          showToolbar = true, previousContext = "settings")
          .apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
          }
      startActivity(intent)
    }
    return true
  }

  override fun setupPreferences() {
    setPermissionPreference()
    setSourceCodePreference()
    setIssueReportPreference()
    setTwitterPreference()
    setTelegramPreference()
    setFacebookPreference()
    setEmailPreference()
    setPrivacyPolicyPreference()
    setTermsConditionsPreference()
    setCreditsPreference()
    setVersionPreference()
    setManageSubscriptionsPreference()
  }

  override fun setVerifiedWalletPreference() {
    val verifyWalletPreference = findPreference<Preference>("pref_verification")
    verifyWalletPreference?.summary =
        getString(R.string.verification_settings_verified_title)
    verifyWalletPreference?.onPreferenceClickListener = null
  }

  override fun setUnverifiedWalletPreference() {
    val verifyWalletPreference = findPreference<Preference>("pref_verification")
    verifyWalletPreference?.summary =
        getString(R.string.verification_settings_unverified_body)
    verifyWalletPreference?.setOnPreferenceClickListener { openWalletValidationScreen() }
  }

  override fun setWalletValidationNoNetwork() {
    val verifyWalletPreference = findPreference<Preference>("pref_verification")
    verifyWalletPreference?.summary =
        getString(
            R.string.verification_settings_no_internet)
    verifyWalletPreference?.shouldDisableView = true
    verifyWalletPreference?.isEnabled = false
    verifyWalletPreference?.let {
      val view = listView.getChildAt(it.order)

      view?.findViewById<AppCompatTextView>(android.R.id.title)
          ?.setTextColor(ResourcesCompat.getColor(resources, R.color.btn_disable_snd_color, null))
      view?.findViewById<AppCompatTextView>(android.R.id.summary)
          ?.setTextColor(
              ResourcesCompat.getColor(resources, R.color.btn_disable_snd_color, null))
    }
    verifyWalletPreference?.setIcon(R.drawable.ic_settings_verification_disabled)
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

  private fun setPermissionPreference() {
    val permissionPreference = findPreference<Preference>("pref_permissions")
    permissionPreference?.setOnPreferenceClickListener {
      openPermissionScreen()
    }
  }

  private fun setManageSubscriptionsPreference() {
    val subscriptionsPreference = findPreference<Preference>("pref_manage_subscriptions")
    subscriptionsPreference?.setOnPreferenceClickListener {
      openSubscriptionsScreen()
    }
  }

  private fun setSourceCodePreference() {
    val sourceCodePreference = findPreference<Preference>("pref_source_code")
    sourceCodePreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://github.com/Aptoide/appcoins-wallet-android"), false)
      false
    }
  }

  private fun setIssueReportPreference() {
    val bugReportPreference = findPreference<Preference>("pref_contact_support")
    bugReportPreference?.setOnPreferenceClickListener {
      supportInteractor.displayChatScreen()
      false
    }
  }

  private fun setTwitterPreference() {
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

  private fun setTelegramPreference() {
    val telegramPreference = findPreference<Preference>("pref_telegram")
    telegramPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://t.me/appcoinsofficial"), false)
      false
    }
  }

  private fun setFacebookPreference() {
    val facebookPreference = findPreference<Preference>("pref_facebook")
    facebookPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://www.facebook.com/AppCoinsOfficial"), false)
      false
    }
  }

  private fun setEmailPreference() {
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
  }

  private fun setPrivacyPolicyPreference() {
    val privacyPolicyPreference = findPreference<Preference>("pref_privacy_policy")
    privacyPolicyPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://catappult.io/appcoins-wallet/privacy-policy"), false)
      false
    }
  }

  private fun setTermsConditionsPreference() {
    val termsConditionsPreference = findPreference<Preference>("pref_terms_condition")
    termsConditionsPreference?.setOnPreferenceClickListener {
      startBrowserActivity(Uri.parse("https://catappult.io/appcoins-wallet/terms-conditions"),
          false)
      false
    }
  }

  private fun setCreditsPreference() {
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

  private fun setVersionPreference() {
    val versionString = getVersion()
    val versionPreference = findPreference<Preference>("pref_version")
    versionPreference?.summary = versionString
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