package com.asfoundation.wallet.ui.splash

import com.asfoundation.wallet.abtesting.experiments.balancewallets.BalanceWalletsExperiment
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.ImpressionPreferencesRepositoryType
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
                               impressionPreferencesRepositoryType: ImpressionPreferencesRepositoryType,
                               balanceWalletsExperiment: BalanceWalletsExperiment): SplashInteractor {
    return SplashInteractor(autoUpdateInteract, balanceWalletsExperiment,
        fingerprintPreferencesRepositoryContract, impressionPreferencesRepositoryType)
  }

  @Provides
  fun providesSplashNavigator(activity: SplashActivity): SplashNavigator {
    return SplashNavigator(activity)
  }
}