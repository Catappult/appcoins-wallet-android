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
  fun providesWalletVerificationActivityPresenter(navigator: VerificationActivityNavigator,
                                                  interactor: VerificationActivityInteractor): VerificationActivityPresenter {
    return VerificationActivityPresenter(navigator, interactor,
        AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable())
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