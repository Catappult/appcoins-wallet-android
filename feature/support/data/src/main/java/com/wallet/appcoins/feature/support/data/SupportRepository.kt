package com.wallet.appcoins.feature.support.data

import android.app.Application
import com.appcoins.wallet.sharedpreferences.OemIdPreferencesDataSource
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
  private val supportPreferences: SupportPreferencesDataSource,
  private val oemIdPreferences: OemIdPreferencesDataSource,
  val app: Application
) {

  companion object {
    private const val USER_LEVEL_ATTRIBUTE = "user_level"
    private const val GAMES_HUB_ATTRIBUTE = "gameshub_installed_carrier"
  }

  private var currentUser: SupportUser = SupportUser()

  fun saveNewUser(walletAddress: String, level: Int) {
    val userAttributes = getUserAttributesBuilder(walletAddress, level)

    val registration: Registration = Registration.create()
      .withUserId(walletAddress)
      .withUserAttributes(userAttributes)

    val gpsAvailable = checkGooglePlayServices()
    if (gpsAvailable) handleFirebaseToken()

    Intercom.client()
      .loginIdentifiedUser(registration)
    currentUser = SupportUser(walletAddress, level)
  }

  fun getSavedUnreadConversations() = supportPreferences.checkSavedUnreadConversations()

  fun updateUnreadConversations(unreadConversations: Int) =
    supportPreferences.updateUnreadConversations(unreadConversations)

  fun resetUnreadConversations() = supportPreferences.resetUnreadConversations()

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

  private fun getUserAttributesBuilder(walletAddress: String, level: Int): UserAttributes {
    val userAttributes = UserAttributes.Builder()
      .withName(walletAddress)
      // We set level + 1 to help with readability for the support team
      .withCustomAttribute(USER_LEVEL_ATTRIBUTE, level + 1)

    if (oemIdPreferences.hasGamesHubOemId())
      userAttributes.withCustomAttribute(
        GAMES_HUB_ATTRIBUTE,
        oemIdPreferences.getGamesHubOemIdIndicative()
      )

    return userAttributes.build()
  }
}
