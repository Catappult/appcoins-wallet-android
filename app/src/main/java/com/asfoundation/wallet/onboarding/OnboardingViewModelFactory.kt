package com.asfoundation.wallet.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OnboardingViewModelFactory() :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return OnboardingViewModel() as T
  }
}