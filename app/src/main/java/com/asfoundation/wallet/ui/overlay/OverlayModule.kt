package com.asfoundation.wallet.ui.overlay

import com.asfoundation.wallet.main.MainActivityNavigator
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class OverlayModule {

  @Provides
  fun providesOverlayPresenter(fragment: OverlayFragment,
                               interactor: OverlayInteractor): OverlayPresenter {
    return OverlayPresenter(fragment as OverlayView, interactor, CompositeDisposable())
  }

  @Provides
  fun providesOverlayInteractor(
      preferencesRepositoryType: PreferencesRepositoryType): OverlayInteractor {
    return OverlayInteractor(preferencesRepositoryType)
  }

  @Provides
  fun providesMainActivityNavigator(fragment: OverlayFragment): MainActivityNavigator {
    return MainActivityNavigator(fragment.requireContext())
  }
}