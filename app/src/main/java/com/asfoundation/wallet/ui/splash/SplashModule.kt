package com.asfoundation.wallet.ui.splash

import androidx.appcompat.app.AppCompatActivity
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(ActivityComponent::class)
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
  fun providesSplashNavigator(activity: AppCompatActivity): SplashNavigator {
    return SplashNavigator(activity)
  }
}