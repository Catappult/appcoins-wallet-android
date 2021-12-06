package com.asfoundation.wallet.onboarding.bottom_sheet

import android.net.Uri
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.GetWalletOrCreateUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import io.reactivex.Completable

sealed class TermsConditionsBottomSheetSideEffect : SideEffect {
  data class NavigateToLink(val uri: Uri) : TermsConditionsBottomSheetSideEffect()
  object NavigateToWalletCreationAnimation : TermsConditionsBottomSheetSideEffect()
  object NavigateBack : TermsConditionsBottomSheetSideEffect()
}

object TermsConditionsBottomSheetState : ViewState

class TermsConditionsBottomSheetViewModel(
    private val getWalletOrCreateUseCase: GetWalletOrCreateUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
    private val rxSchedulers: RxSchedulers) :
    BaseViewModel<TermsConditionsBottomSheetState, TermsConditionsBottomSheetSideEffect>(
        initialState()) {

  companion object {
    fun initialState(): TermsConditionsBottomSheetState {
      return TermsConditionsBottomSheetState
    }
  }

  fun handleDeclineClick() {
    sendSideEffect { TermsConditionsBottomSheetSideEffect.NavigateBack }
  }

  fun handleCreateWallet() {
    getWalletOrCreateUseCase()
        .observeOn(rxSchedulers.main)
        .flatMapCompletable {
          Completable.fromAction {
            sendSideEffect { TermsConditionsBottomSheetSideEffect.NavigateToWalletCreationAnimation }
            setOnboardingCompletedUseCase()
          }
        }
        .scopedSubscribe()
  }

  fun handleLinkClick(uri: Uri) {
    sendSideEffect { TermsConditionsBottomSheetSideEffect.NavigateToLink(uri) }
  }
}