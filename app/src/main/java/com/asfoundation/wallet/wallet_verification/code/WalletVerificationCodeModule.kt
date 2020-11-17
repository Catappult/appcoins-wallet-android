package com.asfoundation.wallet.wallet_verification.code

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
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

}