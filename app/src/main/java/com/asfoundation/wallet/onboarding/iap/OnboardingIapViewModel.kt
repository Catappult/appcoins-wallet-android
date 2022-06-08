package com.asfoundation.wallet.onboarding.iap

import android.content.Intent
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.bottom_sheet.TermsConditionsBottomSheetSideEffect
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class OnboardingIapSideEffect : SideEffect {
  object NavigateToWalletCreationAnimation : OnboardingIapSideEffect()
  object ShowContent : OnboardingIapSideEffect()
}

object OnboardingIapState : ViewState

@HiltViewModel
class OnboardingIapViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase
) : BaseViewModel<OnboardingIapState, OnboardingIapSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingIapState {
      return OnboardingIapState
    }
  }

  fun handleCreateWallet() {
    hasWalletUseCase()
      .observeOn(rxSchedulers.main)
      .doOnSuccess {
        sendSideEffect {
          if (it) {
            OnboardingIapSideEffect.ShowContent
          } else {
            OnboardingIapSideEffect.NavigateToWalletCreationAnimation
          }
        }
      }
      .scopedSubscribe { it.printStackTrace() }
  }

  fun handleUpdateClick() {
  }
}