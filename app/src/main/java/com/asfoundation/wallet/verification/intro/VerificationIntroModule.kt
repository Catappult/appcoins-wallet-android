package com.asfoundation.wallet.verification.intro

import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.verification.VerificationAnalytics
import com.asfoundation.wallet.verification.WalletVerificationInteractor
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class VerificationIntroModule {

  @Provides
  fun providesWalletVerificationIntroNavigator(
      fragment: VerificationIntroFragment): VerificationIntroNavigator {
    return VerificationIntroNavigator(fragment.requireFragmentManager())
  }

  @Provides
  fun providesWalletVerificationIntroPresenter(fragment: VerificationIntroFragment,
                                               navigator: VerificationIntroNavigator,
                                               logger: Logger,
                                               interactor: VerificationIntroInteractor,
                                               data: VerificationIntroData,
                                               analytics: VerificationAnalytics): VerificationIntroPresenter {
    return VerificationIntroPresenter(fragment as VerificationIntroView,
        CompositeDisposable(), navigator, logger, AndroidSchedulers.mainThread(),
        Schedulers.io(), interactor, AdyenErrorCodeMapper(), data, analytics)
  }

  @Provides
  fun provideWalletVerificationIntroInteractor(adyenPaymentRepository: AdyenPaymentRepository,
                                               adyenPaymentInteractor: AdyenPaymentInteractor,
                                               walletService: WalletService,
                                               supportInteractor: SupportInteractor,
                                               walletVerificationInteractor: WalletVerificationInteractor): VerificationIntroInteractor {
    return VerificationIntroInteractor(adyenPaymentRepository, adyenPaymentInteractor,
        walletService, supportInteractor, walletVerificationInteractor)
  }

  @Provides
  fun providesVerificationIntroData(
      fragment: VerificationIntroFragment): VerificationIntroData {
    return VerificationIntroData(RedirectComponent.getReturnUrl(fragment.context!!))
  }

}