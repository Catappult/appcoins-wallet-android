package com.appcoins.wallet.feature.walletInfo.data.wallet

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.authentication.rxOperator.Operators
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class WalletCreatorInteract @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val passwordStore: PasswordStore,
  private val updateWalletNameUseCase: UpdateWalletNameUseCase,
  private val registerTokenUseCase: RegisterFirebaseTokenUseCase,
) {

  fun create(name: String? = null): Single<Wallet> = passwordStore.generatePassword()
    .flatMap { passwordStore.setBackUpPassword(it).toSingleDefault(it) }
    .flatMap { createWallet(it, name) }
    .flatMap { registerTokenUseCase.registerFirebaseToken(it) }

  private fun createWallet(password: String, name: String? = null) =
    walletRepository.createWallet(password)
      .compose(Operators.savePassword(passwordStore, walletRepository, password))
      .flatMap { passwordVerification(it, password) }
      .flatMap { updateWalletNameUseCase(it.address, name).toSingleDefault(it) }

  private fun passwordVerification(wallet: Wallet, masterPassword: String): Single<Wallet> =
    passwordStore.getPassword(wallet.address)
      .flatMap { walletRepository.exportWallet(wallet.address, it, it) }
      .flatMap { walletRepository.findWallet(wallet.address) }
      .onErrorResumeNext { throwable: Throwable? ->
        walletRepository.deleteWallet(wallet.address, masterPassword)
          .lift(Operators.completableErrorProxy(throwable))
          .toSingle { wallet }
      }

  fun setDefaultWallet(address: String): Completable = walletRepository.setDefaultWallet(address)
}
