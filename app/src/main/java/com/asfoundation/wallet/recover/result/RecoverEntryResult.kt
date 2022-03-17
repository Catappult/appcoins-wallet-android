package com.asfoundation.wallet.recover.result

import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import io.reactivex.Single

sealed class RecoverEntryResult

data class SuccessfulEntryRecover(val address: String) : RecoverEntryResult()

sealed class FailedEntryRecover : RecoverEntryResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedEntryRecover()
  data class InvalidPassword(
    val throwable: Throwable? = null,
    val key: String,
    val address: String,
    val amount: String,
    val symbol: String
  ) : FailedEntryRecover()

  data class AlreadyAdded(val throwable: Throwable? = null) : FailedEntryRecover()
  data class InvalidKeystore(val throwable: Throwable? = null) : FailedEntryRecover()
  data class InvalidPrivateKey(val throwable: Throwable? = null) : FailedEntryRecover()
}

class RecoverEntryResultMapper(
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val key: String
) {
  fun map(restoreResult: RestoreResult): Single<RecoverEntryResult> {
    return when (restoreResult) {
      is SuccessfulRestore ->
        Single.just(SuccessfulEntryRecover(restoreResult.address))
      is FailedRestore.AlreadyAdded ->
        Single.just(FailedEntryRecover.AlreadyAdded(restoreResult.throwable))
      is FailedRestore.GenericError ->
        Single.just(FailedEntryRecover.GenericError(restoreResult.throwable))
      is FailedRestore.InvalidKeystore ->
        Single.just(FailedEntryRecover.InvalidKeystore(restoreResult.throwable))
      is FailedRestore.InvalidPassword -> {
        getWalletInfoUseCase(address = restoreResult.address, cached = false, updateFiat = true)
          .map {
            FailedEntryRecover.InvalidPassword(
              throwable = restoreResult.throwable,
              key= key,
              address = it.wallet,
              amount = currencyFormatUtils.formatCurrency(
                it.walletBalance.overallFiat.amount
              ),
              symbol = it.walletBalance.overallFiat.symbol
            )
          }
      }
      is FailedRestore.InvalidPrivateKey ->
        Single.just(FailedEntryRecover.InvalidPrivateKey(restoreResult.throwable))
    }
  }
}
