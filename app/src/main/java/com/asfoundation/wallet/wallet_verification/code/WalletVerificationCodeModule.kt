package com.asfoundation.wallet.wallet_verification.code

import com.asfoundation.wallet.util.CurrencyFormatUtils
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class WalletVerificationCodeModule {

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: WalletVerificationCodeFragment,
                                              currencyFormatUtils: CurrencyFormatUtils): WalletVerificationCodePresenter {
    return WalletVerificationCodePresenter(fragment as WalletVerificationCodeView,
        CompositeDisposable(), Schedulers.io(), Schedulers.computation())
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