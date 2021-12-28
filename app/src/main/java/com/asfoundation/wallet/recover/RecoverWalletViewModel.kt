package com.asfoundation.wallet.recover

import android.net.Uri
import android.util.Log
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.recover.use_cases.*
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase
import io.reactivex.Completable
import io.reactivex.Single

sealed class RecoverWalletSideEffect : SideEffect {
  data class NavigateToFileIntent(val uri: Uri) : RecoverWalletSideEffect()
}

data class RecoverWalletState(
    val recoverResultAsync: Async<RecoverWalletResult> = Async.Uninitialized) :
    ViewState

class RecoverWalletViewModel(private val getFilePathUseCase: GetFilePathUseCase,
                             private val readFileUseCase: ReadFileUseCase,
                             private val setDefaultWalletUseCase: SetDefaultWalletUseCase,
                             private val isKeystoreUseCase: IsKeystoreUseCase,
                             private val recoverKeystoreUseCase: RecoverKeystoreUseCase,
                             private val recoverPrivateKeyUseCase: RecoverPrivateKeyUseCase,
                             private val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
                             private val walletsEventSender: WalletsEventSender,
                             private val rxSchedulers: RxSchedulers) :
    BaseViewModel<RecoverWalletState, RecoverWalletSideEffect>(initialState()) {

  companion object {
    fun initialState(): RecoverWalletState {
      return RecoverWalletState()
    }
  }

  fun filePath(): Uri? {
    return getFilePathUseCase()
  }

  fun handleFileChosen(uri: Uri) {
    Log.d("APPC-2780", "RecoverWalletViewModel: handleFileChosen: uri -> $uri")
    readFileUseCase(uri)
        .observeOn(rxSchedulers.computation)
        .flatMap { fetchWallet(it) }
        .flatMap { setDefaultWalletNew(it) }
        .observeOn(rxSchedulers.main)
        .asAsyncToState {
          copy(recoverResultAsync = it)
        }
        .doOnSuccess { handleRecoverResult(it) }
        .scopedSubscribe()
  }

  private fun fetchWallet(key: String): Single<RecoverWalletResult> {
    return if (isKeystoreUseCase(key = key)) recoverKeystoreUseCase(keystore = key)
    else {
      if (key.length == 64) recoverPrivateKeyUseCase(privateKey = key)
      else Single.just(FailedRecover.InvalidPrivateKey)
    }
  }

  private fun setDefaultWalletNew(recoverResult: RecoverWalletResult): Single<RecoverWalletResult> {
    return when (recoverResult) {
      is FailedRecover -> Single.just(recoverResult)
      is SuccessfulRecover -> Completable.mergeArray(setDefaultWalletUseCase(recoverResult.address),
          updateWalletInfoUseCase(recoverResult.address, updateFiat = true))
          .andThen(Single.just(recoverResult))
    }
  }

  private fun handleRecoverResult(recoverResult: RecoverWalletResult) {
    when (recoverResult) {
      is SuccessfulRecover -> {
        walletsEventSender.sendWalletRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
            WalletsAnalytics.STATUS_SUCCESS)
      }
      is FailedRecover.RequirePassword -> {
        handleRestoreClick("")
        walletsEventSender.sendWalletRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
            WalletsAnalytics.STATUS_SUCCESS)
      }
      else -> {
        walletsEventSender.sendWalletRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
            WalletsAnalytics.STATUS_FAIL, recoverResult.toString())
      }
    }
  }

  fun handleRestoreClick(keystore: String) {

  }

}