package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.authentication.rxOperator.Operators
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
  private val passwordStore: PasswordStore,
  private val walletRepository: WalletRepositoryType,
  private val updateWalletNameUseCase: UpdateWalletNameUseCase,
) {

  operator fun invoke(name: String? = null) =
    passwordStore.generatePassword()
      .flatMap { passwordStore.setBackUpPassword(it).toSingleDefault(it) }
      .flatMap { createWallet(it, name) }

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
}
