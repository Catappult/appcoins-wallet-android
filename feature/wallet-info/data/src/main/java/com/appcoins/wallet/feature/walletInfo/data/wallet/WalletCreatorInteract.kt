package com.appcoins.wallet.feature.walletInfo.data.wallet

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.authentication.rxOperator.Operators
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class WalletCreatorInteract @Inject constructor(
    private val walletRepository: WalletRepositoryType,
    private val passwordStore: PasswordStore,
    private val updateWalletNameUseCase: UpdateWalletNameUseCase,
) {

  fun create(name: String? = null): Single<Wallet> = passwordStore.generatePassword()
    .flatMap { passwordStore.setBackUpPassword(it).toSingleDefault(it) }
    .flatMap {
      walletRepository.createWallet(it)
        .compose(Operators.savePassword(passwordStore, walletRepository, it))
        .flatMap { wallet: Wallet -> passwordVerification(wallet, it) }
        .flatMap { wallet: Wallet ->
          updateWalletNameUseCase(wallet.address, name)
            .toSingleDefault(wallet)
        }
    }

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
