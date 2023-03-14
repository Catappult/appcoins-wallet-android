package com.asfoundation.wallet.eskills.auth

import android.content.Context
import android.content.Intent
import cm.aptoide.skills.interfaces.ExternalAuthenticationProvider
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import fingerprint.FingerprintPreferencesDataSource
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = ExternalAuthenticationProvider::class)
class FingerprintAuthenticationProvider @Inject constructor(
  private val fingerprintPreferences: FingerprintPreferencesDataSource
) :
  ExternalAuthenticationProvider {
  override fun hasAuthenticationPermission(): Boolean {
    return fingerprintPreferences.hasAuthenticationPermission()
  }

  override fun getAuthenticationIntent(context: Context): Intent {
    return AuthenticationPromptActivity.newIntent(context)
      .apply { this.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
  }
}
