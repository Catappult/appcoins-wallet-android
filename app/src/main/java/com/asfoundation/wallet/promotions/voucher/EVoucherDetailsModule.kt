package com.asfoundation.wallet.promotions.voucher

import dagger.Module
import dagger.Provides

@Module
class EVoucherDetailsModule {

  @Provides
  fun providesCarrierFeeNavigator(fragment: EVoucherDetailsFragment): EVoucherDetailsNavigator {
    return EVoucherDetailsNavigator(fragment.requireActivity())
  }

  @Provides
  fun providesEVoucherDetailsData(fragment: EVoucherDetailsFragment): EVoucherDetailsData {
    fragment.arguments!!.apply {
      return EVoucherDetailsData(getString(EVoucherDetailsFragment.TITLE)!!,
          getString(EVoucherDetailsFragment.PACKAGE_NAME)!!)
    }
  }

  @Provides
  fun providesCarrierFeePresenter(fragment: EVoucherDetailsFragment,
                                  navigator: EVoucherDetailsNavigator,
                                  data: EVoucherDetailsData): EVoucherDetailsPresenter {
    return EVoucherDetailsPresenter(fragment, EVoucherDetailsInteractor(), navigator, data)
  }
}