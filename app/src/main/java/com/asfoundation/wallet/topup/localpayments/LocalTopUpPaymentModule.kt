package com.asfoundation.wallet.topup.localpayments

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.core.utils.jvm_common.Logger
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
@Module
class LocalTopUpPaymentModule {

  @Provides
  fun providesLocalTopUpPaymentPresenter(
    fragment: Fragment,
    data: LocalTopUpPaymentData,
    interactor: LocalPaymentInteractor,
    topUpAnalytics: TopUpAnalytics,
    navigator: TopUpNavigator,
    currencyFormatUtils: CurrencyFormatUtils,
    logger: Logger
  ): LocalTopUpPaymentPresenter {
    return LocalTopUpPaymentPresenter(
      fragment as LocalTopUpPaymentView, fragment.context,
      interactor, topUpAnalytics, navigator, currencyFormatUtils, AndroidSchedulers.mainThread(),
      Schedulers.io(), CompositeDisposable(), data, logger
    )
  }

  @Provides
  fun providesLocalTopUpPaymentData(fragment: Fragment) =
    fragment.requireArguments().run {
      LocalTopUpPaymentData(
        paymentId = getString(PAYMENT_ID)!!,
        paymentIcon = getString(PAYMENT_ICON)!!,
        paymentLabel = getString(PAYMENT_LABEL)!!,
        async = getBoolean(ASYNC),
        packageName = getString(PACKAGE_NAME)!!,
        topUpData = getSerializableExtra<TopUpPaymentData>(PAYMENT_DATA)!!
      )
    }
}