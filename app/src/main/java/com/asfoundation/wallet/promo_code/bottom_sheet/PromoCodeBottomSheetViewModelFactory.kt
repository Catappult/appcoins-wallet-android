package com.asfoundation.wallet.promo_code.bottom_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.promo_code.use_cases.DeletePromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.SetPromoCodeUseCase
import io.reactivex.Scheduler

class PromoCodeBottomSheetViewModelFactory(private val networkScheduler: Scheduler,
                                           private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
                                           private val setPromoCodeUseCase: SetPromoCodeUseCase,
                                           private val deletePromoCodeUseCase: DeletePromoCodeUseCase) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return PromoCodeBottomSheetViewModel(networkScheduler, getCurrentPromoCodeUseCase,
        setPromoCodeUseCase, deletePromoCodeUseCase) as T
  }
}