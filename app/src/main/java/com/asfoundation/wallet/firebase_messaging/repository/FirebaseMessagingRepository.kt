package com.asfoundation.wallet.firebase_messaging.repository

import com.asfoundation.wallet.firebase_messaging.repository.model.FirebaseTokenData
import javax.inject.Inject

class FirebaseMessagingRepository @Inject constructor(
  private val firebaseMessagingAPI: FirebaseMessagingAPI,
) {

  fun registerToken(ewtAuthentication: String, token: String) =
    firebaseMessagingAPI.registerToken(
      authorization = ewtAuthentication,
      firebaseTokenData = FirebaseTokenData(
        token = token,
      )
    )

  fun unregisterToken(ewtAuthentication: String, token: String) =
    firebaseMessagingAPI.unregisterToken(
      authorization = ewtAuthentication,
      token = token,
    )
}
