package com.asfoundation.wallet.ui

import android.content.Intent
import io.reactivex.Observable


interface SettingsView {

  fun setRedeemCodePreference(walletAddress: String)

  fun showError()

  fun navigateToIntent(intent: Intent)

  fun authenticationResult(): Observable<Boolean>

  fun disableFingerPrint()

  fun setPermissionPreference()

  fun setSourceCodePreference()

  fun setFingerprintPreference()

  fun setTwitterPreference()

  fun setIssueReportPreference()

  fun setFacebookPreference()

  fun setTelegramPreference()

  fun setEmailPreference()

  fun setPrivacyPolicyPreference()

  fun setTermsConditionsPreference()

  fun setCreditsPreference()

  fun setVersionPreference()

  fun setRestorePreference()

  fun setBackupPreference()

  fun removeFingerprintPreference()

  fun setDisabledFingerPrintPreference()
}