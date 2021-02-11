package com.asfoundation.wallet.promotions.voucher

import dagger.Module
import dagger.Provides

@Module
class EVoucherDetailsModule {

  @Provides
  fun providesCarrierFeeNavigator(fragment: EVoucherDetailsFragment): EVoucherDetailsNavigator {
    return EVoucherDetailsNavigator()
  }

  @Provides
  fun providesCarrierFeePresenter(fragment: EVoucherDetailsFragment,
                                  navigator: EVoucherDetailsNavigator): EVoucherDetailsPresenter {
    return EVoucherDetailsPresenter(fragment, EVoucherDetailsInteractor(), navigator)
  }
}