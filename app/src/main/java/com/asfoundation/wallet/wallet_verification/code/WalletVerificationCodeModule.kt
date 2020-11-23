package com.asfoundation.wallet.wallet_verification.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class WalletVerificationCodeModule {

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: WalletVerificationCodeFragment,
                                              walletVerificationCodeInteractor: WalletVerificationCodeInteractor,
                                              walletVerificationCodeNavigator: WalletVerificationCodeNavigator): WalletVerificationCodePresenter {
    return WalletVerificationCodePresenter(fragment as WalletVerificationCodeView,
        CompositeDisposable(), Schedulers.io(), Schedulers.computation(),
        walletVerificationCodeInteractor, walletVerificationCodeNavigator)
  }

  @Provides
  fun provideWalletVerificationCodeInteractor(adyenPaymentRepository: AdyenPaymentRepository,
                                              walletService: WalletService): WalletVerificationCodeInteractor {
    return WalletVerificationCodeInteractor(adyenPaymentRepository, walletService)
  }

  @Provides
  fun providesWalletVerificationCodeNavigator(
      fragment: WalletVerificationCodeFragment): WalletVerificationCodeNavigator {
    return WalletVerificationCodeNavigator(fragment.requireFragmentManager())
  }

  @Provides
  fun providesVerificationCodeData(fragment: WalletVerificationCodeFragment): VerificationCodeData {
    fragment.arguments!!.apply {
      return VerificationCodeData(
          getLong(WalletVerificationCodeFragment.DATE_KEY),
          getString(WalletVerificationCodeFragment.FORMAT_KEY)!!,
          getString(WalletVerificationCodeFragment.AMOUNT_KEY)!!,
          getString(WalletVerificationCodeFragment.CURRENCY_KEY)!!,
          getString(WalletVerificationCodeFragment.PERIOD_KEY)!!,
          getInt(WalletVerificationCodeFragment.DIGITS_KEY)
      )
    }
  }

}