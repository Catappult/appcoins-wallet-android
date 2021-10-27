package com.asfoundation.wallet.promo_code.bottom_sheet

import com.asfoundation.wallet.promo_code.use_cases.DeletePromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.SetPromoCodeUseCase
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers

@Module
class PromoCodeBottomSheetModule {

  @Provides
  fun providesPromoCodeBottomSheetViewModelFactory(
      getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
      setPromoCodeUseCase: SetPromoCodeUseCase,
      deletePromoCodeUseCase: DeletePromoCodeUseCase): PromoCodeBottomSheetViewModelFactory {
    return PromoCodeBottomSheetViewModelFactory(Schedulers.io(), getCurrentPromoCodeUseCase,
        setPromoCodeUseCase, deletePromoCodeUseCase)
  }

  @Provides
  fun providesPromoCodeBottomSheetNavigator(
      fragment: PromoCodeBottomSheetFragment): PromoCodeBottomSheetNavigator {
    return PromoCodeBottomSheetNavigator(fragment)
  }
}