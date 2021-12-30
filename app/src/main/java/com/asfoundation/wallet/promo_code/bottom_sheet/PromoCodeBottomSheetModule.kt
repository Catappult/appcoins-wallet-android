package com.asfoundation.wallet.promo_code.bottom_sheet

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.promo_code.use_cases.DeletePromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.ObserveCurrentPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.SetPromoCodeUseCase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class PromoCodeBottomSheetModule {

  @Provides
  fun providesPromoCodeBottomSheetViewModelFactory(
      observeCurrentPromoCodeUseCase: ObserveCurrentPromoCodeUseCase,
      setPromoCodeUseCase: SetPromoCodeUseCase,
      deletePromoCodeUseCase: DeletePromoCodeUseCase): PromoCodeBottomSheetViewModelFactory {
    return PromoCodeBottomSheetViewModelFactory(observeCurrentPromoCodeUseCase,
        setPromoCodeUseCase, deletePromoCodeUseCase)
  }

  @Provides
  fun providesPromoCodeBottomSheetNavigator(
      fragment: Fragment): PromoCodeBottomSheetNavigator {
    return PromoCodeBottomSheetNavigator(fragment as BottomSheetDialogFragment)
  }
}