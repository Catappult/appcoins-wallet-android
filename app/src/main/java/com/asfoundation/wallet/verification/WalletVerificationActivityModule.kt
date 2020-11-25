package com.asfoundation.wallet.verification

import com.appcoins.wallet.bdsbilling.WalletService
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class WalletVerificationActivityModule {

  @Provides
  fun providesWalletVerificationActivityPresenter(navigator: WalletVerificationActivityNavigator,
                                                  interactor: WalletVerificationActivityInteractor): WalletVerificationActivityPresenter {
    return WalletVerificationActivityPresenter(navigator, interactor,
        AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable())
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      activity: WalletVerificationActivity): WalletVerificationActivityNavigator {
    return WalletVerificationActivityNavigator(activity, activity.supportFragmentManager)
  }

  @Provides
  fun provideWalletVerificationActivityInteractor(
      walletVerificationRepository: WalletVerificationRepository,
      walletService: WalletService
  ): WalletVerificationActivityInteractor {
    return WalletVerificationActivityInteractor(walletVerificationRepository, walletService)
  }
}