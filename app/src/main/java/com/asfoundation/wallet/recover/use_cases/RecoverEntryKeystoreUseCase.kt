package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.RecoverEntryResultMapper
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import io.reactivex.Single
import javax.inject.Inject

class RecoverEntryKeystoreUseCase @Inject constructor(
        private val walletRepository: WalletRepositoryType,
        private val passwordStore: PasswordStore,
        private val getWalletInfoUseCase: GetWalletInfoUseCase,
        private val currencyFormatUtils: CurrencyFormatUtils
) {

  operator fun invoke(keyStore: WalletKeyStore, password: String = ""): Single<RecoverEntryResult> =
    passwordStore.generatePassword()
      .flatMap { walletRepository.restoreKeystoreToWallet(keyStore.contents, password, it) }
      .flatMap {
        RecoverEntryResultMapper(getWalletInfoUseCase, currencyFormatUtils, keyStore).map(it)
      }
}