package com.appcoins.wallet.feature.walletInfo.data.wallet

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.CreateWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WalletCreatorInteract @Inject constructor(
  private val createWalletUseCase: CreateWalletUseCase,
  private val registerTokenUseCase: RegisterFirebaseTokenUseCase,
  private val setActiveWalletUseCase: SetActiveWalletUseCase,
) {

  fun create(name: String? = null): Single<Wallet> =
    createWalletUseCase(name)
      .subscribeOn(Schedulers.io())
      .flatMap { registerTokenUseCase.registerFirebaseToken(it) }

  fun setDefaultWallet(address: String): Completable = setActiveWalletUseCase(address)
}
