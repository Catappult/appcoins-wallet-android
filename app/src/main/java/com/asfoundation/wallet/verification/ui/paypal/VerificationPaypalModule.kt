package com.asfoundation.wallet.verification.ui.paypal

import com.adyen.checkout.redirect.RedirectComponent
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase
import com.asfoundation.wallet.verification.usecases.SetCachedVerificationUseCase
import dagger.Module
import dagger.Provides

@Module
class VerificationPaypalModule {

  @Provides
  fun provideVerificationPaypalData(
      fragment: VerificationPaypalFragment): VerificationPaypalData {
    return VerificationPaypalData(RedirectComponent.getReturnUrl(fragment.requireContext()))
  }

  @Provides
  fun provideVerificationPaypalViewModelFactory(data: VerificationPaypalData,
                                                getVerificationInfoUseCase: GetVerificationInfoUseCase,
                                                makeVerificationPaymentUseCase: MakeVerificationPaymentUseCase,
                                                setCachedVerificationUseCase: SetCachedVerificationUseCase): VerificationPaypalViewModelFactory {
    return VerificationPaypalViewModelFactory(data, getVerificationInfoUseCase,
        makeVerificationPaymentUseCase, setCachedVerificationUseCase)
  }

  @Provides
  fun provideVerificationPaypalNavigator(
      fragment: VerificationPaypalFragment): VerificationPaypalNavigator {
    return VerificationPaypalNavigator(fragment)
  }
}