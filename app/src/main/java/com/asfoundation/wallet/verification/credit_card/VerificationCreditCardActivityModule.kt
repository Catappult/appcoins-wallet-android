package com.asfoundation.wallet.verification.credit_card

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.verification.credit_card.VerificationCreditCardActivity.Companion.IS_WALLET_VERIFIED
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class VerificationCreditCardActivityModule {

  @Provides
  fun providesVerificationActivityData(
      activity: VerificationCreditCardActivity): VerificationCreditCardActivityData {
    return VerificationCreditCardActivityData(
        activity.intent.getBooleanExtra(IS_WALLET_VERIFIED, false))
  }

  @Provides
  fun providesWalletVerificationActivityPresenter(activity: VerificationCreditCardActivity,
                                                  navigator: VerificationCreditCardActivityNavigator,
                                                  interactor: VerificationCreditCardActivityInteractor,
                                                  analytics: VerificationAnalytics): VerificationCreditCardActivityPresenter {
    return VerificationCreditCardActivityPresenter(activity as VerificationCreditCardActivityView,
        navigator,
        interactor, AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(),
        analytics)
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      activity: VerificationCreditCardActivity): VerificationCreditCardActivityNavigator {
    return VerificationCreditCardActivityNavigator(activity, activity.supportFragmentManager)
  }

  @Provides
  fun provideWalletVerificationActivityInteractor(
      verificationRepository: VerificationRepository,
      walletService: WalletService
  ): VerificationCreditCardActivityInteractor {
    return VerificationCreditCardActivityInteractor(verificationRepository, walletService)
  }
}