package com.asfoundation.wallet.verification.ui.credit_card.intro

import androidx.fragment.app.Fragment
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.verification.ui.credit_card.VerificationAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
@Module
class VerificationIntroModule {

  @Provides
  fun providesWalletVerificationIntroPresenter(fragment: Fragment,
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
  fun providesVerificationIntroData(
      fragment: Fragment): VerificationIntroData {
    return VerificationIntroData(RedirectComponent.getReturnUrl(fragment.requireContext()))
  }

}