package com.asfoundation.wallet.firebase_messaging.domain

interface RegisterFirebaseTokenForWalletsUseCase {

  suspend fun registerFirebaseTokenForAllWallets(token: String, retry: Int = 0, maxRetries: Int = 3)
}