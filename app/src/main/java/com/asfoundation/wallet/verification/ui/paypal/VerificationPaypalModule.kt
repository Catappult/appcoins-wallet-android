package com.asfoundation.wallet.verification.ui.paypal

import androidx.fragment.app.Fragment
import com.adyen.checkout.redirect.RedirectComponent
import com.asfoundation.wallet.verification.usecases.GetVerificationInfoUseCase
import com.asfoundation.wallet.verification.usecases.MakeVerificationPaymentUseCase
import com.asfoundation.wallet.verification.usecases.SetCachedVerificationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class VerificationPaypalModule {

  @Provides
  fun provideVerificationPaypalData(fragment: Fragment): VerificationPaypalData {
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
}