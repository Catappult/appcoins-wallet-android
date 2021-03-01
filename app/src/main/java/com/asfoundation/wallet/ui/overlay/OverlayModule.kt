package com.asfoundation.wallet.ui.overlay

import com.asfoundation.wallet.repository.ImpressionPreferencesRepositoryType
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

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
      impressionPreferencesRepositoryType: ImpressionPreferencesRepositoryType): OverlayInteractor {
    return OverlayInteractor(impressionPreferencesRepositoryType)
  }
}