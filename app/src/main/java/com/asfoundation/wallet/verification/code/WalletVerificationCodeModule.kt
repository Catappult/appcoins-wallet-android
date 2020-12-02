package com.asfoundation.wallet.verification.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.verification.WalletVerificationActivityView
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class WalletVerificationCodeModule {

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: WalletVerificationCodeFragment,
                                              walletVerificationCodeInteractor: WalletVerificationCodeInteractor,
                                              walletVerificationCodeNavigator: WalletVerificationCodeNavigator,
                                              logger: Logger): WalletVerificationCodePresenter {
    return WalletVerificationCodePresenter(fragment as WalletVerificationCodeView,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io(),
        walletVerificationCodeInteractor, walletVerificationCodeNavigator, logger)
  }

  @Provides
  fun provideWalletVerificationCodeInteractor(adyenPaymentRepository: AdyenPaymentRepository,
                                              walletService: WalletService): WalletVerificationCodeInteractor {
    return WalletVerificationCodeInteractor(adyenPaymentRepository, walletService)
  }

  @Provides
  fun providesWalletVerificationCodeNavigator(
      fragment: WalletVerificationCodeFragment): WalletVerificationCodeNavigator {
    return WalletVerificationCodeNavigator(fragment.requireFragmentManager(),
        fragment.activity as WalletVerificationActivityView)
  }

  @Provides
  fun providesVerificationCodeData(fragment: WalletVerificationCodeFragment): VerificationCodeData {
    fragment.arguments!!.apply {
      return VerificationCodeData(
          getBoolean(WalletVerificationCodeFragment.LOADED_KEY),
          getLong(WalletVerificationCodeFragment.DATE_KEY),
          getString(WalletVerificationCodeFragment.FORMAT_KEY),
          getString(WalletVerificationCodeFragment.AMOUNT_KEY),
          getString(WalletVerificationCodeFragment.CURRENCY_KEY),
          getString(WalletVerificationCodeFragment.SYMBOL_KEY),
          getString(WalletVerificationCodeFragment.PERIOD_KEY),
          getInt(WalletVerificationCodeFragment.DIGITS_KEY)
      )
    }
  }

}