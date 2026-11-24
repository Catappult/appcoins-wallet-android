package com.asfoundation.wallet.firebase_messaging.domain

import com.appcoins.wallet.core.network.base.JwtAuthenticatorService
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import com.asfoundation.wallet.firebase_messaging.repository.FirebaseMessagingRepository
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class RegisterFirebaseTokenUseCaseImpl @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val repository: FirebaseMessagingRepository,
  private val firebaseMessaging: FirebaseMessaging,
  private val jwtAuthenticatorService: JwtAuthenticatorService,
) : RegisterFirebaseTokenUseCase, RegisterFirebaseTokenForWalletsUseCase {

  override suspend fun registerFirebaseTokenForAllWallets(
    token: String,
    retry: Int,
    maxRetries: Int
  ) {
    try {
      walletRepository.fetchWallets()
        .flattenAsObservable { it.toList() }
        .flatMapSingle { jwtAuthenticatorService.getJwtAuthenticationWithAddress(it.address) }
        .flatMapCompletable { repository.registerToken(it, token) }
        .await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      if (retry < maxRetries) registerFirebaseTokenForAllWallets(token, retry + 1, maxRetries)
    }
  }

  override fun registerFirebaseToken(wallet: Wallet): Single<Wallet> =
    Single.zip(
      Single.create { emitter ->
        firebaseMessaging.token
          .addOnSuccessListener(emitter::onSuccess)
          .addOnFailureListener(emitter::onError)
      },
      jwtAuthenticatorService.getJwtAuthenticationWithAddress(wallet.address)
    ) { token, jwt -> token to jwt }
      .flatMapCompletable { (token, jwt) ->
        repository.registerToken(
          jwt = jwt,
          token = token
        )
      }
      .onErrorComplete()
      .andThen(Single.just(wallet))

  override fun unregisterFirebaseToken(wallet: Wallet): Completable =
    Single.zip(
      Single.create { emitter ->
        firebaseMessaging.token
          .addOnSuccessListener(emitter::onSuccess)
          .addOnFailureListener(emitter::onError)
      },
      jwtAuthenticatorService.getJwtAuthenticationWithAddress(wallet.address)
    ) { token, jwt -> token to jwt }
      .flatMapCompletable { (token, jwt) ->
        repository.unregisterToken(
          jwt = jwt,
          token = token
        )
      }
      .doOnError { it.printStackTrace() }
      .onErrorComplete()

}


