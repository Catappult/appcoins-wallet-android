package com.asfoundation.wallet.support

import com.asfoundation.wallet.App
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
import io.reactivex.Observable


class SupportInteractor(private val preferences: SupportSharedPreferences, val app: App) {

  companion object {
    private const val USER_LEVEL_ATTRIBUTE = "user_level"
  }

  private var currentUser = ""
  private var currentGamificationLevel = -1

  fun displayChatScreen() {
    resetUnreadConversations()
    Intercom.client()
        .displayMessenger()
  }

  @Suppress("DEPRECATION")
  fun displayConversationListOrChat() {
    //this method was introduced because if the app is closed intercom returns 0 unread conversations
    //even if there are more
    resetUnreadConversations()
    val handledByIntercom = getUnreadConversations() > 0
    if (handledByIntercom) {
      Intercom.client()
          .displayMessenger()
    } else {
      Intercom.client()
          .displayConversationsList()
    }
  }

  fun registerUser(level: Int, walletAddress: String) {
    if (currentUser != walletAddress || currentGamificationLevel != level) {
      if (currentUser != walletAddress) {
        Intercom.client()
            .logout()
      }

      val userAttributes = UserAttributes.Builder()
          .withName(walletAddress)
          .withCustomAttribute(USER_LEVEL_ATTRIBUTE,
              level + 1)//we set level + 1 to help with readability for the support team
          .build()
      val registration: Registration = Registration.create()
          .withUserId(walletAddress)
          .withUserAttributes(userAttributes)

      val gpsAvailable = checkGooglePlayServices()
      if (gpsAvailable) handleFirebaseToken()

      Intercom.client()
          .registerIdentifiedUser(registration)
      currentUser = walletAddress
      currentGamificationLevel = level
    }
  }

  fun getUnreadConversationCountListener() = Observable.create<Int> {
    Intercom.client()
        .addUnreadConversationCountListener { unreadCount -> it.onNext(unreadCount) }
  }

  fun getUnreadConversationCount() = Observable.just(Intercom.client().unreadConversationCount)

  fun shouldShowNotification() =
      getUnreadConversations() > preferences.checkSavedUnreadConversations()

  fun updateUnreadConversations() = preferences.updateUnreadConversations(getUnreadConversations())

  private fun resetUnreadConversations() = preferences.resetUnreadConversations()

  private fun getUnreadConversations() = Intercom.client().unreadConversationCount

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