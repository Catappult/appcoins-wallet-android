package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.SuccessfulRestore
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.RecoverEntryResult
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.RecoverEntryResultMapper
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletKeyStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class RecoverEntryPrivateKeyUseCase @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val passwordStore: PasswordStore,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val registerFirebaseTokenForWalletsUseCase: RegisterFirebaseTokenUseCase,
) {

  operator fun invoke(keyStore: WalletKeyStore): Single<RecoverEntryResult> =
    passwordStore.generatePassword()
      .flatMap { walletRepository.restorePrivateKeyToWallet(keyStore.contents, it) }
      .flatMap { restoreResult ->
        if (restoreResult is SuccessfulRestore) {
          registerFirebaseTokenForWalletsUseCase.registerFirebaseToken(Wallet(restoreResult.address))
            .map { restoreResult }
        } else {
          Single.just(restoreResult)
        }
      }
      .flatMap {
        RecoverEntryResultMapper(getWalletInfoUseCase, currencyFormatUtils, keyStore).map(it)
      }
}