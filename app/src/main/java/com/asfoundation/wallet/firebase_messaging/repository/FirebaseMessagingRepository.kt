package com.asfoundation.wallet.firebase_messaging.repository

import io.reactivex.Completable

interface FirebaseMessagingRepository {

  fun registerToken(ewtAuthentication: String, token: String): Completable

  fun unregisterToken(ewtAuthentication: String, token: String): Completable
}