package com.wallet.appcoins.feature.support.data

import android.app.Application
import com.appcoins.wallet.sharedpreferences.SupportPreferencesDataSource
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.UserAttributes
import io.intercom.android.sdk.identity.Registration
import io.intercom.android.sdk.push.IntercomPushClient
import javax.inject.Inject


class SupportRepository @Inject constructor(
  private val preferences: SupportPreferencesDataSource,
  val app: Application
) {

  companion object {
    private const val USER_LEVEL_ATTRIBUTE = "user_level"
  }

  private var currentUser: SupportUser = SupportUser()

  fun saveNewUser(walletAddress: String, level: Int) {
    val userAttributes = UserAttributes.Builder()
        .withName(walletAddress)
        // We set level + 1 to help with readability for the support team
        .withCustomAttribute(USER_LEVEL_ATTRIBUTE, level + 1)
        .build()
    val registration: Registration = Registration.create()
        .withUserId(walletAddress)
        .withUserAttributes(userAttributes)

    val gpsAvailable = checkGooglePlayServices()
    if (gpsAvailable) handleFirebaseToken()

    Intercom.client()
        .registerIdentifiedUser(registration)
    currentUser = SupportUser(walletAddress, level)
  }

  fun getSavedUnreadConversations() = preferences.checkSavedUnreadConversations()

  fun updateUnreadConversations(unreadConversations: Int) =
      preferences.updateUnreadConversations(unreadConversations)

  fun resetUnreadConversations() = preferences.resetUnreadConversations()

  fun getCurrentUser(): SupportUser = currentUser

  private fun checkGooglePlayServices(): Boolean {
    val availability = GoogleApiAvailability.getInstance()
    return availability.isGooglePlayServicesAvailable(app) == ConnectionResult.SUCCESS
  }

  private fun handleFirebaseToken() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val token = task.result
        if (token != null) {
          IntercomPushClient().sendTokenToIntercom(app, token)
        }
      }
    }
  }
}
