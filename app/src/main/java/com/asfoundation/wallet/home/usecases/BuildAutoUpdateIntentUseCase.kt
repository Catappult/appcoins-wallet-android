package com.asfoundation.wallet.home.usecases

import android.content.Intent
import android.net.Uri
import com.asfoundation.wallet.interact.AutoUpdateInteract

class BuildAutoUpdateIntentUseCase(private val walletPackageName: String) {

  operator fun invoke(): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(retrieveRedirectUrl()))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
  }

  private fun retrieveRedirectUrl(): String {
    return String.format(AutoUpdateInteract.PLAY_APP_VIEW_URL, walletPackageName)
  }
}