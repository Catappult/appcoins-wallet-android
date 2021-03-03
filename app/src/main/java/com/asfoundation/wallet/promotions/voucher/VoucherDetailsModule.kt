package com.asfoundation.wallet.promotions.voucher

import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.promotions.voucher.VoucherDetailsFragment.Companion.FEATURE_GRAPHIC
import com.asfoundation.wallet.promotions.voucher.VoucherDetailsFragment.Companion.ICON
import com.asfoundation.wallet.promotions.voucher.VoucherDetailsFragment.Companion.MAX_BONUS
import com.asfoundation.wallet.promotions.voucher.VoucherDetailsFragment.Companion.PACKAGE_NAME
import com.asfoundation.wallet.promotions.voucher.VoucherDetailsFragment.Companion.TITLE
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class VoucherDetailsModule {

  @Provides
  fun providesVoucherDetailsPresenter(fragment: VoucherDetailsFragment,
                                      navigator: VoucherDetailsNavigator,
                                      interactor: VoucherDetailsInteractor,
                                      data: VoucherDetailsData,
                                      logger: Logger): VoucherDetailsPresenter {
    return VoucherDetailsPresenter(fragment, CompositeDisposable(), interactor, navigator, data,
        AndroidSchedulers.mainThread(), Schedulers.io(), logger)
  }

  @Provides
  fun providesVoucherDetailsNavigator(fragment: VoucherDetailsFragment): VoucherDetailsNavigator {
    return VoucherDetailsNavigator(fragment.requireFragmentManager(), fragment.requireActivity())
  }

  @Provides
  fun providesVoucherDetailsData(fragment: VoucherDetailsFragment): VoucherDetailsData {
    fragment.arguments!!.apply {
      return VoucherDetailsData(
          getString(TITLE)!!,
          getString(FEATURE_GRAPHIC)!!,
          getString(ICON)!!,
          getDouble(MAX_BONUS),
          getString(PACKAGE_NAME)!!,
          getBoolean(VoucherDetailsFragment.HAS_APPCOINS))
    }
  }

  @Provides
  fun providesVoucherDetailsInteractor(): VoucherDetailsInteractor {
    return VoucherDetailsInteractor()
  }
}