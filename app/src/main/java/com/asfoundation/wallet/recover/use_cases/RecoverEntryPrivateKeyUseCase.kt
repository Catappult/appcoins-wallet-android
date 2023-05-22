package com.asfoundation.wallet.recover.use_cases

import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.RecoverEntryResultMapper
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.repository.WalletRepositoryType
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.usecases.GetWalletInfoUseCase
import io.reactivex.Single
import javax.inject.Inject

class RecoverEntryPrivateKeyUseCase @Inject constructor(
    private val walletRepository: com.appcoins.wallet.feature.walletInfo.data.repository.WalletRepositoryType,
    private val passwordStore: PasswordStore,
    private val getWalletInfoUseCase: com.appcoins.wallet.feature.walletInfo.data.usecases.GetWalletInfoUseCase,
    private val currencyFormatUtils: CurrencyFormatUtils
) {

  operator fun invoke(keyStore: WalletKeyStore): Single<RecoverEntryResult> =
    passwordStore.generatePassword()
      .flatMap { walletRepository.restorePrivateKeyToWallet(keyStore.contents, it) }
      .flatMap {
        RecoverEntryResultMapper(getWalletInfoUseCase, currencyFormatUtils, keyStore).map(it)
      }
}