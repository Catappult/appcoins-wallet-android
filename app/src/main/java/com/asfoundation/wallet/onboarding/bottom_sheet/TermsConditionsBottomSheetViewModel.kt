package com.asfoundation.wallet.onboarding.bottom_sheet

import android.net.Uri
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class TermsConditionsBottomSheetSideEffect : SideEffect {
  data class NavigateToLink(val uri: Uri) : TermsConditionsBottomSheetSideEffect()
  object NavigateToWalletCreationAnimation : TermsConditionsBottomSheetSideEffect()
  object NavigateBack : TermsConditionsBottomSheetSideEffect()
}

object TermsConditionsBottomSheetState : ViewState

@HiltViewModel
class TermsConditionsBottomSheetViewModel @Inject constructor(
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
    setOnboardingCompletedUseCase()
    sendSideEffect { TermsConditionsBottomSheetSideEffect.NavigateToWalletCreationAnimation }
  }

  fun handleLinkClick(uri: Uri) {
    sendSideEffect { TermsConditionsBottomSheetSideEffect.NavigateToLink(uri) }
  }
}