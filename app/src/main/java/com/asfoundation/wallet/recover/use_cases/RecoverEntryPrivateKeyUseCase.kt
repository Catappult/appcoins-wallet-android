package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.RecoverEntryResultMapper
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import io.reactivex.Single
import javax.inject.Inject

class RecoverEntryPrivateKeyUseCase @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val passwordStore: PasswordStore,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils
) {

  operator fun invoke(keyStore: WalletKeyStore): Single<RecoverEntryResult> =
    passwordStore.generatePassword()
      .flatMap { walletRepository.restorePrivateKeyToWallet(keyStore.contents, it) }
      .flatMap {
        RecoverEntryResultMapper(getWalletInfoUseCase, currencyFormatUtils, keyStore).map(it)
      }
}