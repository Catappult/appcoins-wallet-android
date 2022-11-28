package com.asfoundation.wallet.update_required.use_cases

import android.content.Intent
import android.net.Uri
import javax.inject.Inject
import javax.inject.Named

class BuildUpdateIntentUseCase @Inject constructor(
  @Named("package-name")
  private val packageName: String,
) {

  companion object {
    const val PLAY_APP_VIEW_URL = "market://details?id=%s"
  }

  operator fun invoke(): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(retrieveRedirectUrl()))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
  }

  private fun retrieveRedirectUrl(): String {
    return String.format(PLAY_APP_VIEW_URL, packageName)
  }
}