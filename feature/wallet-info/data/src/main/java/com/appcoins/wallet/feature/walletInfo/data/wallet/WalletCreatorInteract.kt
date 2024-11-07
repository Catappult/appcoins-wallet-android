package com.appcoins.wallet.feature.walletInfo.data.wallet

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.CreateWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class WalletCreatorInteract @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val createWalletUseCase: CreateWalletUseCase,
  private val registerTokenUseCase: RegisterFirebaseTokenUseCase,
) {

  fun create(name: String? = null): Single<Wallet> =
    createWalletUseCase(name)
      .flatMap { registerTokenUseCase.registerFirebaseToken(it) }

  fun setDefaultWallet(address: String): Completable = walletRepository.setDefaultWallet(address)
}
