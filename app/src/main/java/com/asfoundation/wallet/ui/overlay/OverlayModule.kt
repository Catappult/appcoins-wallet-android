package com.asfoundation.wallet.ui.overlay

import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class OverlayModule {

  @Provides
  fun providesOverlayPresenter(fragment: OverlayFragment): OverlayPresenter {
    return OverlayPresenter(fragment as OverlayView, CompositeDisposable())
  }
}