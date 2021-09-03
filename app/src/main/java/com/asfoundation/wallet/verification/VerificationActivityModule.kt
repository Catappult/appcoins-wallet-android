package com.asfoundation.wallet.verification

import com.appcoins.wallet.bdsbilling.WalletService
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class VerificationActivityModule {

  @Provides
  fun providesWalletVerificationActivityPresenter(activity: VerificationActivity,
                                                  navigator: VerificationActivityNavigator,
                                                  interactor: VerificationActivityInteractor,
                                                  analytics: VerificationAnalytics): VerificationActivityPresenter {
    return VerificationActivityPresenter(activity as VerificationActivityView, navigator,
        interactor, AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(),
        analytics)
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      activity: VerificationActivity): VerificationActivityNavigator {
    return VerificationActivityNavigator(activity, activity.supportFragmentManager)
  }

  @Provides
  fun provideWalletVerificationActivityInteractor(
      verificationRepository: VerificationRepository,
      walletService: WalletService
  ): VerificationActivityInteractor {
    return VerificationActivityInteractor(verificationRepository, walletService)
  }
}