package com.asfoundation.wallet.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.main.usecases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.main.usecases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.support.SupportInteractor

class MainViewModelFactory(private val data: MainData,
                           private val hasSeenPromotionTooltipUseCase: HasSeenPromotionTooltipUseCase,
                           private val increaseLaunchCountUseCase: IncreaseLaunchCountUseCase,
                           private val promotionsInteractor: PromotionsInteractor,
                           private val supportInteractor: SupportInteractor) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return MainViewModel(data, hasSeenPromotionTooltipUseCase, increaseLaunchCountUseCase,
        promotionsInteractor, supportInteractor) as T
  }
}