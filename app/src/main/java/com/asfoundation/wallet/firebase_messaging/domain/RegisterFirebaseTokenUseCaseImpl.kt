package com.asfoundation.wallet.firebase_messaging.domain

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import com.asfoundation.wallet.firebase_messaging.domain.error.RegisterFirebaseMessagingError
import com.asfoundation.wallet.firebase_messaging.repository.FirebaseMessagingRepository
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class RegisterFirebaseTokenUseCaseImpl @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val repository: FirebaseMessagingRepository,
  private val firebaseMessaging: FirebaseMessaging,
  private val ewtAuthenticatorService: EwtAuthenticatorService,
) : RegisterFirebaseTokenUseCase, RegisterFirebaseTokenForWalletsUseCase {

  override suspend fun registerFirebaseTokenForAllWallets(token: String, retry: Int, maxRetries: Int) {
    try {
      walletRepository.fetchWallets()
        .flattenAsObservable { it.toList() }
        .map { ewtAuthenticatorService.getEwtAuthentication(it.address) }
        .flatMapCompletable { repository.registerToken(it, token) }
        .doOnError { throw RegisterFirebaseMessagingError(it) }
        .await()
    } catch (e: RegisterFirebaseMessagingError) {
      registerFirebaseTokenForAllWallets(token, +retry, maxRetries)
    }
  }

  override fun registerFirebaseToken(wallet: Wallet): Single<Wallet> =
    Single.create { emitter ->
      firebaseMessaging.token
        .addOnSuccessListener(emitter::onSuccess)
        .addOnFailureListener(emitter::onError)
    }
      .map { ewtAuthenticatorService.getEwtAuthentication(wallet.address) to it }
      .flatMapCompletable { repository.registerToken(it.first, it.second) }
      .onErrorComplete()
      .andThen(Single.just(wallet))

  override fun unregisterFirebaseToken(wallet: Wallet): Completable =
    Single.create { emitter ->
      firebaseMessaging.token
        .addOnSuccessListener(emitter::onSuccess)
        .addOnFailureListener(emitter::onError)
    }
      .map { ewtAuthenticatorService.getEwtAuthentication(wallet.address) to it }
      .flatMapCompletable { repository.unregisterToken(it.first, it.second) }
      .onErrorComplete()

}
