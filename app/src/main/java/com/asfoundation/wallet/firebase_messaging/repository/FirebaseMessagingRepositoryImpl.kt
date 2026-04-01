package com.asfoundation.wallet.firebase_messaging.repository

import com.asfoundation.wallet.firebase_messaging.repository.model.FirebaseTokenData
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FirebaseMessagingRepositoryImpl @Inject constructor(
  private val firebaseMessagingAPI: FirebaseMessagingAPI,
) : FirebaseMessagingRepository {

  override fun registerToken(jwt: String, token: String): Completable =
    firebaseMessagingAPI.registerToken(
      jwt = jwt,
      firebaseTokenData = FirebaseTokenData(
        token = token,
      )
    )
      .ignoreElement()
      .onErrorComplete()
      .subscribeOn(Schedulers.io())

  override fun unregisterToken(jwt: String, token: String): Completable =
    firebaseMessagingAPI.unregisterToken(
      jwt = jwt,
      token = token,
    )
      .ignoreElement()
      .onErrorComplete()
      .subscribeOn(Schedulers.io())
}
