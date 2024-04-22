package com.wallet.appcoins.feature.support.data

import android.app.Application
import com.appcoins.wallet.core.network.backend.api.SupportApi
import com.appcoins.wallet.core.network.backend.model.IntercomAttributesRequest
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.jvm_common.Logger
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
import io.reactivex.schedulers.Schedulers
import java.util.Locale
import javax.inject.Inject


class SupportRepository @Inject constructor(
  private val oemIdPreferences: OemIdPreferencesDataSource,
  private val app: Application,
  private val ewtAuthenticatorService: EwtAuthenticatorService,
  private val supportApi: SupportApi,
  private val logger: Logger
) {

  companion object {
    private const val TAG = "SupportRepository"
    private const val USER_LEVEL_ATTRIBUTE = "user_level"
    private const val GAMES_HUB_ATTRIBUTE = "gameshub_installed_carrier"
    private const val GAMES_HUB_TAG_CARRIER = "gh_installed_"
    private const val GAMES_HUB_DT_TAG = "gh_dt"
    private const val PAYMENT_CHANNEL_ATTRIBUTE_KEY = "payment_channel"
    private const val ANDROID_CHANNEL_ATTRIBUTE = "appcoins_wallet_android"
  }

  private var currentUser: SupportUser = SupportUser()

  private fun saveNewUser(walletAddress: String, level: Int) {
    val userAttributes = getDefaultUserAttributes(walletAddress, level)

    val registration: Registration =
      Registration.create().withUserId(walletAddress).withUserAttributes(userAttributes)

    val gpsAvailable = checkGooglePlayServices()
    if (gpsAvailable) handleFirebaseToken()

    Intercom.client().loginIdentifiedUser(registration)
    currentUser = SupportUser(walletAddress, level)
  }

  fun hasUnreadConversations() = getUnreadConversations() > 0
  private fun getUnreadConversations() = Intercom.client().unreadConversationCount

  fun openIntercom() {
    val space = if (hasUnreadConversations()) Messages else Home
    sendConversationAttributes(getConversationAttributes())
    Intercom.client().present(space)
  }

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

  fun registerUser(level: Int, walletAddress: String) {
    // force lowercase to make sure 2 users are not registered with the same wallet address, where
    // one has uppercase letters (to be check summed), and the other does not
    val address = walletAddress.lowercase(Locale.ROOT)

    if (currentUser.gamificationLevel != level) Intercom.client()
      .updateUser(getDefaultUserAttributes(address, level))

    if (currentUser.userAddress != address) {
      Intercom.client().logout()
      saveNewUser(walletAddress, level)
    }
  }

  private fun getDefaultUserAttributes(walletAddress: String, level: Int): UserAttributes {
    return UserAttributes.Builder().withName(walletAddress)
      // We set level + 1 to help with readability for the support team
      .withCustomAttribute(USER_LEVEL_ATTRIBUTE, level + 1).build()
  }

  private fun getConversationAttributes(): IntercomAttributesRequest {
    val oemId = oemIdPreferences.getGamesHubOemIdIndicative()
    val attributesMap = mutableMapOf<String, String>()
    val tagsList = mutableListOf<String>()

    attributesMap[PAYMENT_CHANNEL_ATTRIBUTE_KEY] = ANDROID_CHANNEL_ATTRIBUTE

    if (oemIdPreferences.hasGamesHubOemId()) {
      attributesMap[GAMES_HUB_ATTRIBUTE] = oemId
      tagsList.addAll(listOf(GAMES_HUB_DT_TAG, GAMES_HUB_TAG_CARRIER + oemId))
    }

    return IntercomAttributesRequest(tagsList, attributesMap)
  }

  private fun sendConversationAttributes(
    attributes: IntercomAttributesRequest
  ) {
    ewtAuthenticatorService.getEwtAuthentication()
      .flatMap { ewt ->
        supportApi.setConversationAttributesAndTags(ewt, attributes)
      }
      .subscribeOn(Schedulers.io())
      .onErrorReturn {
        logger.log(TAG, it)
        false
      }
      .subscribe()
  }
}
