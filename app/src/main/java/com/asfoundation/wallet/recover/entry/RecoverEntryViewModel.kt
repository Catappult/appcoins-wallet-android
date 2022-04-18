package com.asfoundation.wallet.recover.entry

import android.net.Uri
import com.asfoundation.wallet.base.*
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.recover.use_cases.*
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

sealed class RecoverEntrySideEffect : SideEffect

data class RecoverEntryState(
  val recoverResultAsync: Async<RecoverEntryResult> = Async.Uninitialized
) :
  ViewState

@HiltViewModel
class RecoverEntryViewModel @Inject constructor(
  private val getFilePathUseCase: GetFilePathUseCase,
  private val readFileUseCase: ReadFileUseCase,
  private val setDefaultWalletUseCase: SetDefaultWalletUseCase,
  private val isKeystoreUseCase: IsKeystoreUseCase,
  private val recoverEntryKeystoreUseCase: RecoverEntryKeystoreUseCase,
  private val recoverEntryPrivateKeyUseCase: RecoverEntryPrivateKeyUseCase,
  private val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val walletsEventSender: WalletsEventSender,
  private val rxSchedulers: RxSchedulers
) :
  BaseViewModel<RecoverEntryState, RecoverEntrySideEffect>(initialState()) {

  companion object {
    fun initialState(): RecoverEntryState {
      return RecoverEntryState()
    }
  }

  fun filePath(): Uri? {
    return getFilePathUseCase()
  }

  fun handleFileChosen(uri: Uri) {
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

  private fun fetchWallet(key: String): Single<RecoverEntryResult> {
    return if (isKeystoreUseCase(key = key)) recoverEntryKeystoreUseCase(keystore = key)
    else {
      if (key.length == 64) recoverEntryPrivateKeyUseCase(privateKey = key)
      else Single.just(FailedEntryRecover.InvalidPrivateKey())
    }
  }

  private fun setDefaultWallet(recoverResult: RecoverEntryResult): Single<RecoverEntryResult> {
    return when (recoverResult) {
      is FailedEntryRecover -> Single.just(recoverResult)
      is SuccessfulEntryRecover -> Completable.mergeArray(
        setDefaultWalletUseCase(recoverResult.address),
        updateWalletInfoUseCase(recoverResult.address, updateFiat = true)
      ).andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
        .andThen(Single.just(recoverResult))
    }
  }

  fun handleRecoverClick(keystore: String) {
    fetchWallet(keystore)
      .flatMap { setDefaultWallet(it) }
      .asAsyncToState {
        copy(recoverResultAsync = it)
      }
      .doOnSuccess { handleRecoverResult(it) }
      .doOnError {
        walletsEventSender.sendWalletCompleteRestoreEvent(
          WalletsAnalytics.STATUS_FAIL,
          it.message
        )
      }
      .scopedSubscribe()
  }

  private fun handleRecoverResult(recoverResult: RecoverEntryResult) {
    when (recoverResult) {
      is SuccessfulEntryRecover -> {
        walletsEventSender.sendWalletRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_SUCCESS
        )
      }
      is FailedEntryRecover.InvalidPassword -> {
        setState { copy(recoverResultAsync = Async.Uninitialized) }
      }
      else -> {
        walletsEventSender.sendWalletRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_FAIL, recoverResult.toString()
        )
      }
    }
  }
}