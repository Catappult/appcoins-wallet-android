package com.asfoundation.wallet.onboarding.pending_payment

import androidx.lifecycle.viewModelScope
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.gp_install.OnboardingGPInstallSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingPaymentSideEffect : SideEffect {
  data class ShowHeaderContent(
    val packageName: String,
    val sku: String,
    val currency: String,
    val value: Double
  ) : OnboardingPaymentSideEffect()
}

object OnboardingPaymentState : ViewState

@HiltViewModel
class OnboardingPaymentViewModel @Inject constructor(
  private val appStartUseCase: AppStartUseCase
) :
  BaseViewModel<OnboardingPaymentState, OnboardingPaymentSideEffect>(OnboardingPaymentState) {

  init {
    handleHeader()
  }

  private fun handleHeader() {
    viewModelScope.launch {
      val mode = appStartUseCase.startModes.first() as StartMode.PendingPurchaseFlow
      sendSideEffect {
        OnboardingPaymentSideEffect.ShowHeaderContent(
          mode.packageName,
          mode.sku,
          mode.currency!!,
          mode.value!!
        )
      }
    }
  }

}