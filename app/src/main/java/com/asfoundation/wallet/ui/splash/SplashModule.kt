package com.asfoundation.wallet.ui.splash

import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class SplashModule {

  @Provides
  fun providesSplashPresenter(interactor: SplashInteractor,
                              navigator: SplashNavigator): SplashPresenter {
    return SplashPresenter(interactor, navigator, AndroidSchedulers.mainThread(), Schedulers.io(),
        CompositeDisposable())
  }

  @Provides
  fun providesSplashInteractor(autoUpdateInteract: AutoUpdateInteract,
                               fingerprintPreferencesRepositoryContract: FingerprintPreferencesRepositoryContract,
                               preferencesRepositoryType: PreferencesRepositoryType): SplashInteractor {
    return SplashInteractor(autoUpdateInteract,
        fingerprintPreferencesRepositoryContract, preferencesRepositoryType)
  }

  @Provides
  fun providesSplashNavigator(activity: SplashActivity): SplashNavigator {
    return SplashNavigator(activity)
  }
}