package com.asfoundation.wallet.wallet_verification.intro

import com.asfoundation.wallet.logging.Logger
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class WalletVerificationIntroModule {

  @Provides
  fun providesWalletVerificationIntroNavigator(
      fragment: WalletVerificationIntroFragment): WalletVerificationIntroNavigator {
    return WalletVerificationIntroNavigator(fragment.requireFragmentManager())
  }

  @Provides
  fun providesWalletVerificationIntroPresenter(fragment: WalletVerificationIntroFragment,
                                               navigator: WalletVerificationIntroNavigator,
                                               logger: Logger,
                                               interactor: WalletVerificationIntroInteractor): WalletVerificationIntroPresenter {
    return WalletVerificationIntroPresenter(fragment as WalletVerificationIntroView,
        CompositeDisposable(), navigator, logger, AndroidSchedulers.mainThread(),
        Schedulers.io(), interactor)
  }

}