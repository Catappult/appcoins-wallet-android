package com.asfoundation.wallet.recover.entry

import android.net.Uri
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.arch.*
import com.appcoins.wallet.ui.arch.data.Async
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.recover.use_cases.*
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase
import com.asfoundation.wallet.wallets.usecases.UpdateWalletNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

sealed class RecoverEntrySideEffect : SideEffect

data class RecoverEntryState(
  val recoverResultAsync: Async<RecoverEntryResult> = Async.Uninitialized
) : ViewState

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
  private val updateBackupStateFromRecoverUseCase: UpdateBackupStateFromRecoverUseCase,
  private val updateWalletNameUseCase: UpdateWalletNameUseCase,
  private val walletsEventSender: WalletsEventSender,
  private val rxSchedulers: RxSchedulers
) : BaseViewModel<RecoverEntryState, RecoverEntrySideEffect>(initialState()) {

  companion object {
    fun initialState(): RecoverEntryState = RecoverEntryState()
  }

  fun filePath(): Uri? = getFilePathUseCase()

  fun handleFileChosen(uri: Uri) {
    readFileUseCase(uri)
      .observeOn(rxSchedulers.computation)
      .flatMap { fetchWallet(it) }
      .flatMap { setDefaultWallet(it) }
      .observeOn(rxSchedulers.main)
      .asAsyncToState { copy(recoverResultAsync = it) }
      .doOnSuccess { handleRecoverResult(it) }
      .scopedSubscribe()
  }

  private fun fetchWallet(keyStore: WalletKeyStore): Single<RecoverEntryResult> =
    when {
      isKeystoreUseCase(key = keyStore.contents) -> recoverEntryKeystoreUseCase(keyStore = keyStore)
      keyStore.contents.length == 64 -> recoverEntryPrivateKeyUseCase(keyStore = keyStore)
      else -> Single.just(FailedEntryRecover.InvalidPrivateKey())
    }

  private fun setDefaultWallet(recoverResult: RecoverEntryResult): Single<RecoverEntryResult> =
    when (recoverResult) {
      is FailedEntryRecover -> Single.just(recoverResult)
      is SuccessfulEntryRecover -> setDefaultWalletUseCase(recoverResult.address)
        .mergeWith(updateWalletInfoUseCase(recoverResult.address))
        .andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
        .andThen(updateWalletNameUseCase(recoverResult.address, recoverResult.name))
        .toSingleDefault(recoverResult)
    }

  fun handleRecoverClick(keystore: String) {
    fetchWallet(WalletKeyStore(null, keystore))
      .flatMap { setDefaultWallet(it) }
      .asAsyncToState { copy(recoverResultAsync = it) }
      .doOnSuccess { handleRecoverResult(it) }
      .doOnError {
        walletsEventSender.sendWalletCompleteRestoreEvent(
          WalletsAnalytics.STATUS_FAIL,
          it.message
        )
      }
      .scopedSubscribe()
  }

  private fun handleRecoverResult(recoverResult: RecoverEntryResult) =
    when (recoverResult) {
      is SuccessfulEntryRecover -> {
        updateWalletBackupState()
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

  private fun updateWalletBackupState() {
    updateBackupStateFromRecoverUseCase().scopedSubscribe()
  }
}
