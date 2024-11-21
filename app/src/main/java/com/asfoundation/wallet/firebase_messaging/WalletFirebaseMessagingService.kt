package com.asfoundation.wallet.firebase_messaging

import com.appcoins.wallet.core.analytics.analytics.logging.Log
import com.asfoundation.wallet.di.IoDispatcher
import com.asfoundation.wallet.firebase_messaging.domain.RegisterFirebaseTokenUseCaseImpl
import com.asfoundation.wallet.support.IntercomNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WalletFirebaseMessagingService : FirebaseMessagingService() {

  companion object {
    private const val TAG = "WalletFirebaseMessaging"
  }

  @Inject
  lateinit var registerFirebaseMessagingTokenUseCase: RegisterFirebaseTokenUseCaseImpl


  @Inject
  lateinit var pushNotification: PushNotification


  @Inject
  lateinit var intercomNotification: IntercomNotification

  @Inject
  @IoDispatcher
  lateinit var networkDispatcher: CoroutineDispatcher

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
    Log.e(TAG, "From: ${remoteMessage.from}")

    when {
      intercomNotification.isIntercomPush(remoteMessage) ->
        intercomNotification.sendNotification()

      else -> showNotification(remoteMessage)
    }
  }

  private fun showNotification(remoteMessage: RemoteMessage) {
    pushNotification.sendPushNotification(remoteMessage)
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.e(TAG, "Token: $token")
    CoroutineScope(networkDispatcher).launch {
      registerFirebaseMessagingTokenUseCase.registerFirebaseTokenForAllWallets(token)

      intercomNotification.sendTokenToIntercom(token)
    }
  }

}
