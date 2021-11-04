package com.asfoundation.wallet.verification.credit_card.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.verification.credit_card.*
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class VerificationCodeModule {

  @Provides
  fun providesVerificationActivityData(
      fragment: VerificationCodeFragment): VerificationCreditCardActivityData {
    return VerificationCreditCardActivityData(
        (fragment.activity as VerificationCreditCardActivity).intent.getBooleanExtra(
            VerificationCreditCardActivity.IS_WALLET_VERIFIED, false))
  }

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: VerificationCodeFragment,
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
  fun providesWalletVerificationCodeNavigator(fragment: VerificationCodeFragment,
                                              activityNavigator: VerificationCreditCardActivityNavigator): VerificationCodeNavigator {
    return VerificationCodeNavigator(fragment.requireFragmentManager(),
        fragment.activity as VerificationCreditCardActivityView, activityNavigator)
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      fragment: VerificationCodeFragment): VerificationCreditCardActivityNavigator {
    return VerificationCreditCardActivityNavigator(fragment.requireActivity(),
        fragment.requireActivity().supportFragmentManager)
  }

  @Provides
  fun providesVerificationCodeData(fragment: VerificationCodeFragment): VerificationCodeData {
    fragment.arguments!!.apply {
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