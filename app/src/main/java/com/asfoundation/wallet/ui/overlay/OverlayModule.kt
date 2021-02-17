package com.asfoundation.wallet.ui.overlay

import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyData
import com.asfoundation.wallet.ui.iab.payments.carrier.verify.CarrierVerifyFragment
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

@Module
class OverlayModule {

  @Provides
  fun providesOverlayData(fragment: OverlayFragment): OverlayData {
    fragment.arguments!!.apply {
      return OverlayData(getInt(OverlayFragment.BOTTOM_NAVIGATION_ITEM_KEY),
          getSerializable(OverlayFragment.OVERLAY_TYPE_KEY) as OverlayType)
    }
  }

  @Provides
  fun providesOverlayPresenter(fragment: OverlayFragment,
                               data: OverlayData,
                               interactor: OverlayInteractor): OverlayPresenter {
    return OverlayPresenter(fragment as OverlayView, data, interactor, CompositeDisposable())
  }

  @Provides
  fun providesOverlayInteractor(
      preferencesRepositoryType: PreferencesRepositoryType): OverlayInteractor {
    return OverlayInteractor(preferencesRepositoryType)
  }
}