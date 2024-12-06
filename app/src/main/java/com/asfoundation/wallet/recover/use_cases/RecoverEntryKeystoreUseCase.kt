package com.asfoundation.wallet.recover.use_cases

import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.SuccessfulRestore
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.RecoverEntryResultMapper
import io.reactivex.Single
import javax.inject.Inject

class RecoverEntryKeystoreUseCase @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val passwordStore: PasswordStore,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val registerFirebaseTokenForWalletsUseCase: RegisterFirebaseTokenUseCase,
) {

  operator fun invoke(keyStore: WalletKeyStore, password: String = ""): Single<RecoverEntryResult> =
    passwordStore.generatePassword()
      .flatMap { walletRepository.restoreKeystoreToWallet(keyStore.contents, password, it) }
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