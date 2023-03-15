package com.asfoundation.wallet.verification.ui.credit_card

import android.app.Activity
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity.Companion.IS_WALLET_VERIFIED
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(ActivityComponent::class)
@Module
class VerificationCreditCardActivityModule {

  @Provides
  fun providesVerificationActivityData(
    activity: Activity
  ): VerificationCreditCardActivityData {
    return VerificationCreditCardActivityData(
      activity.intent.getBooleanExtra(IS_WALLET_VERIFIED, false)
    )
  }

  @Provides
  fun providesWalletVerificationActivityPresenter(
    activity: Activity,
    navigator: VerificationCreditCardActivityNavigator,
    interactor: VerificationCreditCardActivityInteractor,
    rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers,
    analytics: VerificationAnalytics
  ): VerificationCreditCardActivityPresenter {
    return VerificationCreditCardActivityPresenter(
      activity as VerificationCreditCardActivityView,
      navigator, interactor, rxSchedulers, CompositeDisposable(),
      analytics
    )
  }
}