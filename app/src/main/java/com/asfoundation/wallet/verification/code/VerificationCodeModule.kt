package com.asfoundation.wallet.verification.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.verification.*
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class VerificationCodeModule {

  @Provides
  fun providesVerificationActivityData(
      fragment: VerificationCodeFragment): VerificationActivityData {
    return VerificationActivityData(
        (fragment.activity as VerificationActivity).intent.getBooleanExtra(
            VerificationActivity.IS_WALLET_VERIFIED, false))
  }

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: VerificationCodeFragment,
                                              data: VerificationCodeData,
                                              activityData: VerificationActivityData,
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
      adyenPaymentRepository: AdyenPaymentRepository,
      walletService: WalletService): VerificationCodeInteractor {
    return VerificationCodeInteractor(walletVerificationInteractor, adyenPaymentRepository,
        walletService)
  }

  @Provides
  fun providesWalletVerificationCodeNavigator(fragment: VerificationCodeFragment,
                                              activityNavigator: VerificationActivityNavigator): VerificationCodeNavigator {
    return VerificationCodeNavigator(fragment.requireFragmentManager(),
        fragment.activity as VerificationActivityView, activityNavigator)
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      fragment: VerificationCodeFragment): VerificationActivityNavigator {
    return VerificationActivityNavigator(fragment.requireActivity(),
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