package com.asfoundation.wallet.verification.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.verification.VerificationActivityNavigator
import com.asfoundation.wallet.verification.VerificationActivityView
import com.asfoundation.wallet.verification.VerificationAnalytics
import com.asfoundation.wallet.verification.WalletVerificationInteractor
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class VerificationCodeModule {

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: VerificationCodeFragment,
                                              data: VerificationCodeData,
                                              verificationCodeInteractor: VerificationCodeInteractor,
                                              verificationCodeNavigator: VerificationCodeNavigator,
                                              logger: Logger,
                                              analytics: VerificationAnalytics): VerificationCodePresenter {
    return VerificationCodePresenter(fragment as VerificationCodeView, data,
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