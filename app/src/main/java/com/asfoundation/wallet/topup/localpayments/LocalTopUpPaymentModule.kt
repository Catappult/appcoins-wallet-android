package com.asfoundation.wallet.topup.localpayments

import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.adyen.TopUpNavigator
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment.Companion.ASYNC
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment.Companion.PACKAGE_NAME
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment.Companion.PAYMENT_DATA
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment.Companion.PAYMENT_ICON
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment.Companion.PAYMENT_ID
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment.Companion.PAYMENT_LABEL
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class LocalTopUpPaymentModule {

  @Provides
  fun providesLocalTopUpPaymentPresenter(fragment: LocalTopUpPaymentFragment,
                                         data: LocalTopUpPaymentData,
                                         interactor: LocalPaymentInteractor,
                                         topUpAnalytics: TopUpAnalytics,
                                         navigator: TopUpNavigator,
                                         currencyFormatUtils: CurrencyFormatUtils,
                                         logger: Logger): LocalTopUpPaymentPresenter {
    return LocalTopUpPaymentPresenter(fragment as LocalTopUpPaymentView, fragment.context,
        interactor, topUpAnalytics, navigator, currencyFormatUtils, AndroidSchedulers.mainThread(),
        Schedulers.io(), CompositeDisposable(), data, logger)
  }

  @Provides
  fun providesLocalTopUpPaymentData(fragment: LocalTopUpPaymentFragment): LocalTopUpPaymentData {
    fragment.arguments!!.apply {
      return LocalTopUpPaymentData(getString(PAYMENT_ID)!!, getString(PAYMENT_ICON)!!,
          getString(PAYMENT_LABEL)!!, getBoolean(ASYNC), getString(PACKAGE_NAME)!!,
          getSerializable(PAYMENT_DATA) as TopUpPaymentData)
    }
  }

  @Provides
  fun providesTopUpPaymentNavigator(fragment: LocalTopUpPaymentFragment): TopUpNavigator {
    return TopUpNavigator(fragment.requireFragmentManager(), fragment.activity as UriNavigator,
        fragment.activity as TopUpActivityView)
  }
}