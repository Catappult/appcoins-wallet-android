package com.asfoundation.wallet.promotions.voucher

import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.vouchers.VouchersRepository
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
          getString(VoucherDetailsFragment.TITLE)!!,
          getString(VoucherDetailsFragment.FEATURE_GRAPHIC)!!,
          getString(VoucherDetailsFragment.ICON)!!,
          getDouble(VoucherDetailsFragment.MAX_BONUS),
          getString(VoucherDetailsFragment.PACKAGE_NAME)!!,
          getBoolean(VoucherDetailsFragment.HAS_APPCOINS))
    }
  }

  @Provides
  fun providesVoucherDetailsInteractor(repository: VouchersRepository): VoucherDetailsInteractor {
    return VoucherDetailsInteractor(repository)
  }
}