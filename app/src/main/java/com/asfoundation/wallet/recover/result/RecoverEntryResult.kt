package com.asfoundation.wallet.recover.result

import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.FailedRestore
import com.appcoins.wallet.feature.walletInfo.data.RestoreResult
import com.appcoins.wallet.feature.walletInfo.data.SuccessfulRestore
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.asfoundation.wallet.entity.WalletKeyStore
import io.reactivex.Single

sealed class RecoverEntryResult

data class SuccessfulEntryRecover(val address: String, val name: String?) : RecoverEntryResult()

sealed class FailedEntryRecover : RecoverEntryResult() {
  object GenericError : FailedEntryRecover()
  data class InvalidPassword(
    val keyStore: WalletKeyStore,
    val address: String,
    val amount: String,
    val name: String?,
    val symbol: String
  ) : FailedEntryRecover()

  object AlreadyAdded : FailedEntryRecover()
  object InvalidKeystore : FailedEntryRecover()
  object InvalidPrivateKey : FailedEntryRecover()
}

class RecoverEntryResultMapper(
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val walletKeyStore: WalletKeyStore
) {
  fun map(restoreResult: RestoreResult): Single<RecoverEntryResult> = when (restoreResult) {
    is SuccessfulRestore ->
      Single.just(SuccessfulEntryRecover(restoreResult.address, walletKeyStore.name))

    is FailedRestore.AlreadyAdded ->
      Single.just(FailedEntryRecover.AlreadyAdded)

    is FailedRestore.GenericError ->
      Single.just(FailedEntryRecover.GenericError)

    is FailedRestore.InvalidKeystore ->
      Single.just(FailedEntryRecover.InvalidKeystore)

    is FailedRestore.InvalidPassword -> getWalletInfoUseCase(
      address = restoreResult.address,
      cached = false
    )
      .map {
        FailedEntryRecover.InvalidPassword(
          keyStore = walletKeyStore,
          address = it.wallet,
          amount = currencyFormatUtils.formatCurrency(it.walletBalance.overallFiat.amount),
          name = it.name,
          symbol = it.walletBalance.overallFiat.symbol
        )
      }

    is FailedRestore.InvalidPrivateKey ->
      Single.just(FailedEntryRecover.InvalidPrivateKey)
  }
}
