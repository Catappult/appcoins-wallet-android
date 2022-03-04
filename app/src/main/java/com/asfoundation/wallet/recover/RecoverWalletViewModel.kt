package com.asfoundation.wallet.recover

import android.net.Uri
import android.util.Log
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.recover.use_cases.*
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

sealed class RecoverWalletSideEffect : SideEffect {
  data class NavigateToFileIntent(val uri: Uri) : RecoverWalletSideEffect()
}

data class RecoverWalletState(
    val recoverResultAsync: Async<RecoverWalletResult> = Async.Uninitialized) :
    ViewState

@HiltViewModel
class RecoverWalletViewModel @Inject constructor(private val getFilePathUseCase: GetFilePathUseCase,
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
        .flatMap { setDefaultWallet(it) }
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
      else Single.just(FailedWalletRecover.InvalidPrivateKey())
    }
  }

  private fun setDefaultWallet(recoverResult: RecoverWalletResult): Single<RecoverWalletResult> {
    return when (recoverResult) {
      is FailedWalletRecover -> Single.just(recoverResult)
      is SuccessfulWalletRecover -> Completable.mergeArray(
          setDefaultWalletUseCase(recoverResult.address),
          updateWalletInfoUseCase(recoverResult.address, updateFiat = true))
          .andThen(Single.just(recoverResult))
    }
  }

  private fun handleRecoverResult(recoverResult: RecoverWalletResult) {
    when (recoverResult) {
      is SuccessfulWalletRecover -> {
        walletsEventSender.sendWalletRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
            WalletsAnalytics.STATUS_SUCCESS)
      }
      is FailedWalletRecover.RequirePassword -> {
//        handleRecoverClick("")
        walletsEventSender.sendWalletRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
            WalletsAnalytics.STATUS_SUCCESS)
      }
      else -> {
        walletsEventSender.sendWalletRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
            WalletsAnalytics.STATUS_FAIL, recoverResult.toString())
      }
    }
  }

  fun handleRecoverClick(keystore: String, passwordRequired: Boolean) {
    fetchWallet(keystore)
        .doOnSuccess {
          if (passwordRequired){
            walletsEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
                WalletsAnalytics.STATUS_SUCCESS)
          }
        }
        .doOnError {
          if (passwordRequired){
            walletsEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
                WalletsAnalytics.STATUS_FAIL, it.message)
          }
        }
        .flatMap { setDefaultWallet(it) }
        .asAsyncToState {
          copy(recoverResultAsync = it)
        }
        .doOnSuccess { handleRecoverResult(it) }
        .doOnError {
          walletsEventSender.sendWalletCompleteRestoreEvent(WalletsAnalytics.STATUS_FAIL,
              it.message)
        }
        .scopedSubscribe()
  }
}