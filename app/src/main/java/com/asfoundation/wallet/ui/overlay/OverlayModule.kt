package com.asfoundation.wallet.ui.overlay

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class OverlayModule {

  @Provides
  fun providesOverlayPresenter(fragment: Fragment,
                               interactor: OverlayInteractor): OverlayPresenter {
    return OverlayPresenter(fragment as OverlayView, interactor, CompositeDisposable())
  }
}