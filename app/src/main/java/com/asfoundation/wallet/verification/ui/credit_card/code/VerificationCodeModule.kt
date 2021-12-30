package com.asfoundation.wallet.verification.ui.credit_card.code

import androidx.fragment.app.Fragment
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.verification.repository.VerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
@Module
class VerificationCodeModule {

//  @Provides
//  fun providesVerificationActivityData(fragment: Fragment): VerificationCreditCardActivityData {
//    return VerificationCreditCardActivityData(
//        (fragment.activity as VerificationCreditCardActivity).intent.getBooleanExtra(
//            VerificationCreditCardActivity.IS_WALLET_VERIFIED, false))
//  }

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: Fragment,
                                              data: VerificationCodeData,
                                              activityData: VerificationCreditCardActivityData,
                                              verificationCodeInteractor: VerificationCodeInteractor,
                                              verificationCodeNavigator: VerificationCodeNavigator,
                                              logger: Logger,
                                              analytics: VerificationAnalytics): VerificationCodePresenter {
    return VerificationCodePresenter(fragment as VerificationCodeView, data, activityData,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io(),
        verificationCodeInteractor, verificationCodeNavigator, logger, analytics)
  }

  @Provides
  fun provideWalletVerificationCodeInteractor(
      walletVerificationInteractor: WalletVerificationInteractor,
      verificationRepository: VerificationRepository,
      walletService: WalletService): VerificationCodeInteractor {
    return VerificationCodeInteractor(walletVerificationInteractor, verificationRepository,
        walletService)
  }

  @Provides
  fun providesWalletVerificationCodeNavigator(fragment: Fragment,
                                              activityNavigator: VerificationCreditCardActivityNavigator): VerificationCodeNavigator {
    return VerificationCodeNavigator(fragment.requireFragmentManager(),
        fragment.activity as VerificationCreditCardActivityView, activityNavigator)
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      fragment: Fragment): VerificationCreditCardActivityNavigator {
    return VerificationCreditCardActivityNavigator(fragment.requireActivity(),
        fragment.requireActivity().supportFragmentManager)
  }

  @Provides
  fun providesVerificationCodeData(fragment: Fragment): VerificationCodeData {
    fragment.requireArguments()
        .apply {
          return VerificationCodeData(getBoolean(VerificationCodeFragment.LOADED_KEY),
              getLong(VerificationCodeFragment.DATE_KEY),
              getString(VerificationCodeFragment.FORMAT_KEY),
              getString(VerificationCodeFragment.AMOUNT_KEY),
              getString(VerificationCodeFragment.CURRENCY_KEY),
              getString(VerificationCodeFragment.SYMBOL_KEY),
              getString(VerificationCodeFragment.PERIOD_KEY),
              getInt(VerificationCodeFragment.DIGITS_KEY)
          )
        }
  }
}