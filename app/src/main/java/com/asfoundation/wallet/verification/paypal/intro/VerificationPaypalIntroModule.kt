package com.asfoundation.wallet.verification.paypal.intro

import com.adyen.checkout.redirect.RedirectComponent
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase
import dagger.Module
import dagger.Provides

@Module
class VerificationPaypalIntroModule {

  @Provides
  fun provideVerificationPaypalIntroData(
      fragment: VerificationPaypalIntroFragment): VerificationPaypalIntroData {
    return VerificationPaypalIntroData(RedirectComponent.getReturnUrl(fragment.requireContext()))
  }

  @Provides
  fun provideVerificationPaypalIntroViewModelFactory(
      data: VerificationPaypalIntroData,
      getVerificationInfoUseCase: GetVerificationInfoUseCase,
      makeVerificationPaymentUseCase: MakeVerificationPaymentUseCase
  ): VerificationPaypalIntroViewModelFactory {
    return VerificationPaypalIntroViewModelFactory(data, getVerificationInfoUseCase,
        makeVerificationPaymentUseCase)
  }

  @Provides
  fun provideVerificationPaypalIntroNavigator(
      fragment: VerificationPaypalIntroFragment): VerificationPaypalIntroNavigator {
    return VerificationPaypalIntroNavigator(fragment)
  }
}