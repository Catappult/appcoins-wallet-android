package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import io.reactivex.Completable
import io.reactivex.Single

interface RegisterFirebaseTokenUseCase {
  fun registerFirebaseToken(wallet: Wallet): Single<Wallet>
  fun unregisterFirebaseToken(wallet: Wallet): Completable
}