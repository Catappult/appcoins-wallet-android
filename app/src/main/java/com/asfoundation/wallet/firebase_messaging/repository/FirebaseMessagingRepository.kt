package com.asfoundation.wallet.firebase_messaging.repository

import io.reactivex.Completable

interface FirebaseMessagingRepository {

  fun registerToken(jwt: String, token: String): Completable

  fun unregisterToken(jwt: String, token: String): Completable
}