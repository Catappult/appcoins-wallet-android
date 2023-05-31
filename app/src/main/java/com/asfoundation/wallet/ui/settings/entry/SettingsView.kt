package com.asfoundation.wallet.ui.settings.entry

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrency
import io.reactivex.Observable


interface SettingsView {
  fun showError()

  fun navigateToIntent(intent: Intent)

  fun authenticationResult(): ActivityResultLauncher<Intent>

  fun toggleFingerprint(enabled: Boolean)

  fun setPermissionPreference()

  fun setSourceCodePreference()

  fun setFingerprintPreference(hasAuthenticationPermission: Boolean)

  fun setTwitterPreference()

  fun setIssueReportPreference()

  fun setFacebookPreference()

  fun setTelegramPreference()

  fun setEmailPreference()

  fun setPrivacyPolicyPreference()

  fun setTermsConditionsPreference()

  fun setCreditsPreference()

  fun setVersionPreference()

  fun setCurrencyPreference(selectedCurrency: FiatCurrency)

  fun setManageWalletPreference()

  fun setAccountPreference()

  fun setManageSubscriptionsPreference()

  fun removeFingerprintPreference()

  fun setDisabledFingerPrintPreference()

  fun switchPreferenceChange(): Observable<Unit>

  fun updateFingerPrintListener(enabled: Boolean)

  fun setFaqsPreference()
}