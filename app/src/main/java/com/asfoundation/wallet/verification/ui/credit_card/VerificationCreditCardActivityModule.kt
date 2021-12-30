package com.asfoundation.wallet.verification.ui.credit_card

import androidx.appcompat.app.AppCompatActivity
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.verification.repository.VerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity.Companion.IS_WALLET_VERIFIED
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(ActivityComponent::class)
@Module
class VerificationCreditCardActivityModule {

  @Provides
  fun providesVerificationActivityData(
      activity: AppCompatActivity): VerificationCreditCardActivityData {
    return VerificationCreditCardActivityData(
        activity.intent.getBooleanExtra(IS_WALLET_VERIFIED, false))
  }

  @Provides
  fun providesWalletVerificationActivityPresenter(activity: AppCompatActivity,
                                                  navigator: VerificationCreditCardActivityNavigator,
                                                  interactor: VerificationCreditCardActivityInteractor,
                                                  analytics: VerificationAnalytics): VerificationCreditCardActivityPresenter {
    return VerificationCreditCardActivityPresenter(activity as VerificationCreditCardActivityView,
        navigator,
        interactor, AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(),
        analytics)
  }

//  @Provides
//  fun providesWalletVerificationActivityNavigator(
//      activity: AppCompatActivity): VerificationCreditCardActivityNavigator {
//    return VerificationCreditCardActivityNavigator(activity, activity.supportFragmentManager)
//  }

  @Provides
  fun provideWalletVerificationActivityInteractor(
      verificationRepository: VerificationRepository,
      walletService: WalletService
  ): VerificationCreditCardActivityInteractor {
    return VerificationCreditCardActivityInteractor(verificationRepository, walletService)
  }
}