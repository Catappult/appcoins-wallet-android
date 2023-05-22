package com.asfoundation.wallet.support

import android.app.Application
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.UserAttributes
import io.intercom.android.sdk.identity.Registration
import io.intercom.android.sdk.push.IntercomPushClient
import com.appcoins.wallet.sharedpreferences.SupportPreferencesDataSource
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
    FirebaseInstanceId.getInstance()
        .instanceId
        .addOnCompleteListener(object : OnCompleteListener<InstanceIdResult?> {
          override fun onComplete(task: Task<InstanceIdResult?>) {
            if (!task.isSuccessful) return
            IntercomPushClient().sendTokenToIntercom(app, task.result?.token!!)
          }
        })
  }
}