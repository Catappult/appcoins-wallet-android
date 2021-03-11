package com.asfoundation.wallet.ui.iab.vouchers

import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class VouchersSuccessModule {

  @Provides
  fun providesVouchersSuccessPresenter(fragment: VouchersSuccessFragment,
                                       data: VouchersSuccessData,
                                       navigator: VouchersSuccessNavigator): VouchersSuccessPresenter {
    return VouchersSuccessPresenter(fragment as VouchersSuccessView, CompositeDisposable(), data,
        navigator)
  }

  @Provides
  fun providesVouchersSuccessData(fragment: VouchersSuccessFragment): VouchersSuccessData {
    fragment.arguments!!.apply {
      return VouchersSuccessData(getString(VouchersSuccessFragment.BONUS_KEY, ""),
          getString(VouchersSuccessFragment.CODE_KEY, ""),
          getString(VouchersSuccessFragment.REDEEM_LINK_KEY, ""))
    }
  }

  @Provides
  fun providesVouchersNavigator(fragment: VouchersSuccessFragment): VouchersSuccessNavigator {
    return VouchersSuccessNavigator(fragment.activity!!)
  }
}