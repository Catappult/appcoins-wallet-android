package com.asfoundation.wallet.onboarding

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.UpdateWalletNameUseCase
import com.asfoundation.wallet.analytics.SaveIsFirstPaymentUseCase
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.main.use_cases.DeleteCachedGuestWalletUseCase
import com.asfoundation.wallet.main.use_cases.GetBonusGuestWalletUseCase
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_OSP
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_SDK
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.recover.result.FailedEntryRecover
import com.asfoundation.wallet.recover.result.RecoverEntryResult
import com.asfoundation.wallet.recover.result.SuccessfulEntryRecover
import com.asfoundation.wallet.recover.use_cases.RecoverEntryPrivateKeyUseCase
import com.asfoundation.wallet.recover.use_cases.SetDefaultWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingSideEffect : SideEffect {
  data class NavigateToLink(val uri: Uri) : OnboardingSideEffect()
  data class NavigateToWalletCreationAnimation(val isPayment: Boolean) : OnboardingSideEffect()
  object NavigateToRecoverWallet : OnboardingSideEffect()
  object NavigateToFinish : OnboardingSideEffect()
  object ShowLoadingRecover : OnboardingSideEffect()
  object NavigateToOnboardingPayment : OnboardingSideEffect()
  data class UpdateGuestBonus(val bonus: FiatValue) : OnboardingSideEffect()
  data class NavigateToVerify(val flow: String) : OnboardingSideEffect()
  object OpenLogin : OnboardingSideEffect()
}

data class OnboardingState(
  val pageContent: OnboardingContent = OnboardingContent.EMPTY,
  val walletCreationAsync: Async<Unit> = Async.Uninitialized
) : ViewState

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val recoverEntryPrivateKeyUseCase: RecoverEntryPrivateKeyUseCase,
  private val setDefaultWalletUseCase: SetDefaultWalletUseCase,
  private val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
  private val updateWalletNameUseCase: UpdateWalletNameUseCase,
  private val getBonusGuestWalletUseCase: GetBonusGuestWalletUseCase,
  private val deleteCachedGuestWalletUseCase: DeleteCachedGuestWalletUseCase,
  private val walletsEventSender: WalletsEventSender,
  private val onboardingAnalytics: OnboardingAnalytics,
  private val saveIsFirstPaymentUseCase: SaveIsFirstPaymentUseCase,
  appStartUseCase: AppStartUseCase
) : BaseViewModel<OnboardingState, OnboardingSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingState {
      return OnboardingState()
    }
  }

  private var guestBonus = FiatValue()

  init {
    handleLaunchMode(appStartUseCase)
  }

  private fun handleLaunchMode(appStartUseCase: AppStartUseCase) {
    viewModelScope.launch {
      when (appStartUseCase.startModes.first()) {
        is StartMode.PendingPurchaseFlow -> {
          if ((appStartUseCase.startModes.first() as StartMode.PendingPurchaseFlow).type == PAYMENT_TYPE_OSP) {
            sendSideEffect {
              OnboardingSideEffect.NavigateToWalletCreationAnimation(isPayment = true)
            }
          } else if ((appStartUseCase.startModes.first() as StartMode.PendingPurchaseFlow).type == PAYMENT_TYPE_SDK) {
            sendSideEffect { OnboardingSideEffect.ShowLoadingRecover }
          }
        }

        is StartMode.GPInstall -> sendSideEffect {
          OnboardingSideEffect.NavigateToWalletCreationAnimation(isPayment = false)
        }

        else -> setState { copy(pageContent = OnboardingContent.VALUES) }
      }
    }
  }

  fun handleLaunchWalletClick() {
    hasWalletUseCase().observeOn(rxSchedulers.main).doOnSuccess {
//      setOnboardingCompletedUseCase()   // TODO set this after login is done
      sendSideEffect {
        if (it) {
//          OnboardingSideEffect.NavigateToFinish
          OnboardingSideEffect.OpenLogin  //TODO send wallet is exists
        } else {
//          OnboardingSideEffect.NavigateToWalletCreationAnimation(isPayment = false)
          OnboardingSideEffect.OpenLogin
        }
      }
    }.scopedSubscribe { it.printStackTrace() }
  }

  fun handleRecoverClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToRecoverWallet }
  }

  fun handleLinkClick(uri: Uri) {
    sendSideEffect { OnboardingSideEffect.NavigateToLink(uri) }
  }

  fun handleRecoverAndVerifyGuestWalletClick(backupModel: BackupModel) {
    sendSideEffect { OnboardingSideEffect.ShowLoadingRecover }
    recoverEntryPrivateKeyUseCase(
      WalletKeyStore(
        name = null,
        contents = backupModel.backupPrivateKey
      )
    )
      .flatMap { setDefaultWallet(it) }
      .doOnSuccess {
        handleRecoverResult(
          recoverResult = it,
          flow = backupModel.flow,
          paymentFunnel = backupModel.paymentFunnel,
        )
      }
      .doOnError {
        handleRecoverResult(
          recoverResult = FailedEntryRecover.GenericError(),
          flow = backupModel.flow,
          paymentFunnel = backupModel.paymentFunnel,
        )
        walletsEventSender.sendWalletCompleteRestoreEvent(
          status = WalletsAnalytics.STATUS_FAIL,
          errorDetails = it.message
        )
      }.scopedSubscribe()
  }

  private fun setDefaultWallet(recoverResult: RecoverEntryResult): Single<RecoverEntryResult> =
    when (recoverResult) {
      is FailedEntryRecover -> Single.just(recoverResult)
      is SuccessfulEntryRecover -> setDefaultWalletUseCase(recoverResult.address).mergeWith(
        updateWalletInfoUseCase(recoverResult.address)
      ).andThen(Completable.fromAction { setOnboardingCompletedUseCase() })
        .andThen(updateWalletNameUseCase(recoverResult.address, recoverResult.name))
        .toSingleDefault(recoverResult)
    }

  private fun handleRecoverResult(
    recoverResult: RecoverEntryResult,
    flow: String,
    paymentFunnel: String?
  ) =
    when (recoverResult) {
      is SuccessfulEntryRecover -> {
        walletsEventSender.sendWalletRestoreEvent(
          action = WalletsAnalytics.ACTION_IMPORT,
          status = WalletsAnalytics.STATUS_SUCCESS
        )
        deleteCachedGuest()
        saveIsFirstPaymentUseCase(
          isFirstPayment = paymentFunnel == null || paymentFunnel.equals("first_payment_try", true) || paymentFunnel.equals("first_payment", true)
        )
        onboardingAnalytics.sendRecoverGuestWalletEvent(
          bonus = guestBonus.amount.toString(),
          bonusCurrency = guestBonus.currency
        )
        if (flow.isEmpty()) sendSideEffect { OnboardingSideEffect.NavigateToFinish }
        else handleFlowTypes(flow)
      }

      is FailedEntryRecover.InvalidPassword -> {
      }

      else -> {
        walletsEventSender.sendWalletRestoreEvent(
          WalletsAnalytics.ACTION_IMPORT, WalletsAnalytics.STATUS_FAIL, recoverResult.toString()
        )
      }
    }

  private fun handleFlowTypes(flow: String) {
    sendSideEffect {
      when (flow) {
        OnboardingFlow.ONBOARDING_PAYMENT.name -> OnboardingSideEffect.NavigateToOnboardingPayment
        OnboardingFlow.VERIFY_PAYPAL.name, OnboardingFlow.VERIFY_CREDIT_CARD.name ->
          OnboardingSideEffect.NavigateToVerify(flow)

        else -> OnboardingSideEffect.NavigateToFinish
      }
    }
  }

  fun getGuestWalletBonus(key: String) {
    getBonusGuestWalletUseCase(key).doOnSuccess {
      guestBonus = it
      sendSideEffect { OnboardingSideEffect.UpdateGuestBonus(it) }
    }.doOnError {
      walletsEventSender.sendWalletCompleteRestoreEvent(
        WalletsAnalytics.STATUS_FAIL, "getGuestWalletBonus: ${it.message}"
      )
    }.scopedSubscribe()
  }

  private fun deleteCachedGuest() {
    deleteCachedGuestWalletUseCase().scopedSubscribe()
  }

}