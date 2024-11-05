package com.asfoundation.wallet.firebase_messaging.repository

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.asfoundation.wallet.firebase_messaging.repository.model.FirebaseTokenData
import io.reactivex.Single
import javax.inject.Inject

class FirebaseMessagingRepository @Inject constructor(
  private val firebaseMessagingAPI: FirebaseMessagingAPI,
  private val ewtAuthenticatorService: EwtAuthenticatorService,
) {

  fun registerToken(wallet: String, token: String) =
    Single.just(ewtAuthenticatorService.getEwtAuthentication(wallet))
      .flatMapCompletable {
        firebaseMessagingAPI.registerToken(
          authorization = it,
          firebaseTokenData = FirebaseTokenData(
            token = token
          )
        )
      }

  fun unregisterToken(wallet: String, token: String) =
    Single.just(ewtAuthenticatorService.getEwtAuthentication(wallet))
      .flatMapCompletable {
        firebaseMessagingAPI.unregisterToken(
          authorization = it,
          firebaseTokenData = FirebaseTokenData(
            token = token
          )
        )
      }
}
