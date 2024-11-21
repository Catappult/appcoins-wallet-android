package com.asfoundation.wallet.firebase_messaging.repository

import com.asfoundation.wallet.firebase_messaging.repository.model.FirebaseTokenData
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(FirebaseMessagingRepository::class)
class FirebaseMessagingRepositoryImpl @Inject constructor(
  private val firebaseMessagingAPI: FirebaseMessagingAPI,
) : FirebaseMessagingRepository {

  override fun registerToken(ewtAuthentication: String, token: String): Completable =
    firebaseMessagingAPI.registerToken(
      authorization = ewtAuthentication,
      firebaseTokenData = FirebaseTokenData(
        token = token,
      )
    )
      .ignoreElement()
      .onErrorComplete()
      .subscribeOn(Schedulers.io())

  override fun unregisterToken(ewtAuthentication: String, token: String): Completable =
    firebaseMessagingAPI.unregisterToken(
      authorization = ewtAuthentication,
      token = token,
    )
      .ignoreElement()
      .onErrorComplete()
      .subscribeOn(Schedulers.io())
}
