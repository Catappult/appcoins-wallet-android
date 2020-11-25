package com.asfoundation.wallet.verification.intro

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.support.SupportInteractor
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
        Schedulers.io(), interactor, AdyenErrorCodeMapper())
  }

  @Provides
  fun provideWalletVerificationIntroInteractor(
      adyenPaymentRepository: AdyenPaymentRepository,
      adyenPaymentInteractor: AdyenPaymentInteractor,
      walletService: WalletService,
      supportInteractor: SupportInteractor
  ): WalletVerificationIntroInteractor {
    return WalletVerificationIntroInteractor(adyenPaymentRepository, adyenPaymentInteractor,
        walletService, supportInteractor)
  }

}