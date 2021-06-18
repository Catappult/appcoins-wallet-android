package com.asfoundation.wallet.promotions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenWalletOriginUseCase
import io.reactivex.Scheduler

/**
 * Migrate to Hilt to remove this boilerplate
 */
class PromotionsViewModelFactory(private val getPromotionsUseCase: GetPromotionsUseCase,
                                 private val analyticsSetup: AnalyticsSetup,
                                 private val setSeenPromotionsUseCase: SetSeenPromotionsUseCase,
                                 private val setSeenWalletOriginUseCase: SetSeenWalletOriginUseCase,
                                 private val networkScheduler: Scheduler) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return PromotionsViewModel(getPromotionsUseCase, analyticsSetup, setSeenPromotionsUseCase,
        setSeenWalletOriginUseCase, networkScheduler) as T
  }
}