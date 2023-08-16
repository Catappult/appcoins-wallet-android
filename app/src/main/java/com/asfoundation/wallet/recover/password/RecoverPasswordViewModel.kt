package com.asfoundation.wallet.recover.password

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.password.RecoverPasswordFragment.Companion.KEYSTORE_KEY
import com.asfoundation.wallet.recover.result.*
import com.asfoundation.wallet.recover.use_cases.*
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase
import com.asfoundation.wallet.wallets.usecases.UpdateWalletNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

sealed class RecoverPasswordSideEffect : SideEffect

data class RecoverPasswordState(
  val recoverResultAsync: Async<RecoverPasswordResult> = Async.Uninitialized
) :
  ViewState

@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
  private val setDefaultWalletUseCase: SetDefaultWalletUseCase,
  private val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
  private val walletsEventSender: WalletsEventSender,
  private val recoverPasswordKeystoreUseCase: RecoverPasswordKeystoreUseCase,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val updateBackupStateFromRecoverUseCase: UpdateBackupStateFromRecoverUseCase,
  private val updateWalletNameUseCase: UpdateWalletNameUseCase,
  private val savedStateHandle: SavedStateHandle,
) :
  com.appcoins.wallet.ui.arch.BaseViewModel<RecoverPasswordState, RecoverPasswordSideEffect>(initialState()) {

  companion object {
    fun initialState(): RecoverPasswordState {
      return RecoverPasswordState()
    }
  }

  private fun setDefaultWallet(recoverResult: RecoverPasswordResult): Single<RecoverPasswordResult> {
    return when (recoverResult) {
      is FailedPasswordRecover -> Single.just(recoverResult)
      is SuccessfulPasswordRecover -> Completable.mergeArray(
        setDefaultWalletUseCase(recoverResult.address),
        updateWalletInfoUseCase(recoverResult.address)
      ).andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
        .andThen(updateWalletNameUseCase(recoverResult.address, recoverResult.name))
        .andThen(Single.just(recoverResult))
    }
  }

  fun handleRecoverPasswordClick(password: String) {
    val keystore = savedStateHandle.get<WalletKeyStore>(KEYSTORE_KEY)
    recoverPasswordKeystoreUseCase(keyStore = keystore!!, password = password)
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

  private fun handleRecoverResult(recoverResult: RecoverPasswordResult) {
    when (recoverResult) {
      is SuccessfulPasswordRecover -> {
        updateWalletBackupState()
        walletsEventSender.sendWalletPasswordRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_SUCCESS
        )
      }
      is FailedPasswordRecover.InvalidPassword -> {
        walletsEventSender.sendWalletPasswordRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_FAIL, recoverResult.throwable?.message
        )
      }
      else -> {
        walletsEventSender.sendWalletRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_FAIL, recoverResult.toString()
        )
      }
    }
  }

  private fun updateWalletBackupState() {
    updateBackupStateFromRecoverUseCase()
      .scopedSubscribe()
  }
}