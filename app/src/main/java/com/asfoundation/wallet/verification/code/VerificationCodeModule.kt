package com.asfoundation.wallet.verification.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.verification.VerificationActivityView
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class VerificationCodeModule {

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: VerificationCodeFragment,
                                              verificationCodeInteractor: VerificationCodeInteractor,
                                              verificationCodeNavigator: VerificationCodeNavigator,
                                              logger: Logger): VerificationCodePresenter {
    return VerificationCodePresenter(fragment as VerificationCodeView,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io(),
        verificationCodeInteractor, verificationCodeNavigator, logger)
  }

  @Provides
  fun provideWalletVerificationCodeInteractor(adyenPaymentRepository: AdyenPaymentRepository,
                                              walletService: WalletService): VerificationCodeInteractor {
    return VerificationCodeInteractor(adyenPaymentRepository, walletService)
  }

  @Provides
  fun providesWalletVerificationCodeNavigator(
      fragment: VerificationCodeFragment): VerificationCodeNavigator {
    return VerificationCodeNavigator(fragment.requireFragmentManager(),
        fragment.activity as VerificationActivityView)
  }

  @Provides
  fun providesVerificationCodeData(fragment: VerificationCodeFragment): VerificationCodeData {
    fragment.arguments!!.apply {
      return VerificationCodeData(
          getBoolean(VerificationCodeFragment.LOADED_KEY),
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