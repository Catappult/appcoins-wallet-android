package com.wallet.appcoins.feature.support.data

import android.app.Application
import com.appcoins.wallet.core.network.backend.api.SupportApi
import com.appcoins.wallet.core.network.backend.model.IntercomAttributesRequest
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.sharedpreferences.AppStartPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.OemIdPreferencesDataSource
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.IntercomSpace.Home
import io.intercom.android.sdk.IntercomSpace.Messages
import io.intercom.android.sdk.UserAttributes
import io.intercom.android.sdk.identity.Registration
import io.intercom.android.sdk.push.IntercomPushClient
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SupportRepository @Inject constructor(
  private val appStartPreferencesDataSource: AppStartPreferencesDataSource,
  private val oemIdPreferences: OemIdPreferencesDataSource,
  private val app: Application,
  private val supportApi: SupportApi,
  private val logger: Logger,
  private val firebaseMessaging: FirebaseMessaging,
) {

  companion object {
    private const val TAG = "SupportRepository"
    private const val USER_LEVEL_ATTRIBUTE_KEY = "user_level"
    private const val GAMES_HUB_ATTRIBUTE_KEY = "gameshub_installed_carrier"
    private const val UID_ATTRIBUTE_KEY = "uid"
    private const val GAMES_HUB_TAG_CARRIER = "gh_installed_"
    private const val GAMES_HUB_DT_TAG = "gh_dt"
    private const val PAYMENT_CHANNEL_ATTRIBUTE_KEY = "payment_channel"
    private const val PAYMENT_FUNNEL_ATTRIBUTE_KEY = "payment_funnel"
    private const val ANDROID_CHANNEL_ATTRIBUTE = "android"
    private const val FIRST_PAYMENT_ATTRIBUTE = "first_payment"
    private const val REGULAR_PAYMENT_ATTRIBUTE = "regular_payment"
  }

  private fun saveNewUser(walletAddress: String, level: Int) {
    val userAttributes = getDefaultUserAttributes(walletAddress, level)

    val registration: Registration =
      Registration.create().withUserId(walletAddress).withUserAttributes(userAttributes)

    val gpsAvailable = checkGooglePlayServices()
    if (gpsAvailable) handleFirebaseToken()

    Intercom.client().loginIdentifiedUser(registration)
  }

  fun hasUnreadConversations() = getUnreadConversations() > 0
  private fun getUnreadConversations(): Int {
    return Intercom.client().unreadConversationCount
  }

  fun openIntercom(uid: String? = null) {
    val space = if (hasUnreadConversations()) Messages else Home
    sendConversationAttributes(getConversationAttributes(uid))
    Intercom.client().present(space)
  }

  private fun checkGooglePlayServices(): Boolean {
    val availability = GoogleApiAvailability.getInstance()
    return availability.isGooglePlayServicesAvailable(app) == ConnectionResult.SUCCESS
  }

  private fun handleFirebaseToken() {
    firebaseMessaging.token.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val token = task.result
        if (token != null) {
          IntercomPushClient().sendTokenToIntercom(app, token)
        }
      }
    }
  }

  fun updateUser(walletAddress: String, level: Int): Completable =
    Completable.create {
      Intercom.client().updateUser(getDefaultUserAttributes(walletAddress, level))
      it.onComplete()
    }

  fun changeUser(walletAddress: String, level: Int): Completable =
    Completable.create {
      Intercom.client().logout()
      saveNewUser(walletAddress, level)
      it.onComplete()
    }

  private fun getDefaultUserAttributes(walletAddress: String, level: Int): UserAttributes {
    return UserAttributes.Builder().withName(walletAddress)
      // We set level + 1 to help with readability for the support team
      .withCustomAttribute(USER_LEVEL_ATTRIBUTE_KEY, level + 1).build()
  }

  private fun getConversationAttributes(
    uid: String?
  ): IntercomAttributesRequest {
    val oemId = oemIdPreferences.getGamesHubOemIdIndicative()
    val attributesMap = mutableMapOf<String, String>()
    val tagsList = mutableListOf<String>()

    attributesMap[PAYMENT_CHANNEL_ATTRIBUTE_KEY] = ANDROID_CHANNEL_ATTRIBUTE

    if (!uid.isNullOrEmpty()) attributesMap[UID_ATTRIBUTE_KEY] = uid

    if (appStartPreferencesDataSource.getIsFirstPayment()) {
      tagsList.add(FIRST_PAYMENT_ATTRIBUTE)
      attributesMap[PAYMENT_FUNNEL_ATTRIBUTE_KEY] = FIRST_PAYMENT_ATTRIBUTE
    } else {
      attributesMap[PAYMENT_FUNNEL_ATTRIBUTE_KEY] = REGULAR_PAYMENT_ATTRIBUTE
    }

    if (oemIdPreferences.hasGamesHubOemId()) {
      attributesMap[GAMES_HUB_ATTRIBUTE_KEY] = oemId
      tagsList.addAll(listOf(GAMES_HUB_DT_TAG, GAMES_HUB_TAG_CARRIER + oemId))
    }

    return IntercomAttributesRequest(tagsList, attributesMap)
  }

  private fun sendConversationAttributes(
    attributes: IntercomAttributesRequest
  ) {
    supportApi.setConversationAttributesAndTags(attributes)
      .subscribeOn(Schedulers.io())
      .onErrorReturn {
        logger.log(TAG, it)
        false
      }
      .subscribe()
  }
}
