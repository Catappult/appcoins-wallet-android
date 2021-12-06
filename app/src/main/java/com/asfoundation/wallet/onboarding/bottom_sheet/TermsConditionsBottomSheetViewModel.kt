package com.asfoundation.wallet.onboarding.bottom_sheet

import android.net.Uri
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase

sealed class TermsConditionsBottomSheetSideEffect : SideEffect {
  data class NavigateToLink(val uri: Uri) : TermsConditionsBottomSheetSideEffect()
  object NavigateToWalletCreationAnimation : TermsConditionsBottomSheetSideEffect()
  object NavigateBack : TermsConditionsBottomSheetSideEffect()
}

object TermsConditionsBottomSheetState : ViewState

class TermsConditionsBottomSheetViewModel(
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase) :
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
    sendSideEffect { TermsConditionsBottomSheetSideEffect.NavigateToWalletCreationAnimation }
    setOnboardingCompletedUseCase()
  }

  fun handleLinkClick(uri: Uri) {
    sendSideEffect { TermsConditionsBottomSheetSideEffect.NavigateToLink(uri) }
  }
}