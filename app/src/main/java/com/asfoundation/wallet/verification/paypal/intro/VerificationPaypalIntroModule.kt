package com.asfoundation.wallet.verification.paypal.intro

import com.adyen.checkout.redirect.RedirectComponent
import com.asfoundation.wallet.verification.credit_card.WalletVerificationInteractor
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
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
      walletVerificationInteractor: WalletVerificationInteractor
  ): VerificationPaypalIntroViewModelFactory {
    return VerificationPaypalIntroViewModelFactory(data, getVerificationInfoUseCase,
        walletVerificationInteractor)
  }

  @Provides
  fun provideVerificationPaypalIntroNavigator(
      fragment: VerificationPaypalIntroFragment): VerificationPaypalIntroNavigator {
    return VerificationPaypalIntroNavigator(fragment)
  }
}