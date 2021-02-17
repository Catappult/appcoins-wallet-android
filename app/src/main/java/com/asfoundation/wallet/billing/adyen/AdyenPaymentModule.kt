package com.asfoundation.wallet.billing.adyen

import com.adyen.checkout.redirect.RedirectComponent
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.ui.iab.IabNavigator
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.Navigator
import com.asfoundation.wallet.ui.iab.TransactionPaymentData
import com.asfoundation.wallet.util.CurrencyFormatUtils
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class AdyenPaymentModule {

  @Provides
  fun providesAdyenPaymentPresenter(fragment: AdyenPaymentFragment,
                                    data: AdyenPaymentData,
                                    analytics: AdyenPaymentAnalytics,
                                    interactor: AdyenPaymentInteractor,
                                    navigator: Navigator,
                                    servicesErrorCodeMapper: ServicesErrorCodeMapper,
                                    currencyFormatUtils: CurrencyFormatUtils,
                                    logger: Logger): AdyenPaymentPresenter {
    return AdyenPaymentPresenter(fragment as AdyenPaymentView, CompositeDisposable(),
        AndroidSchedulers.mainThread(), Schedulers.io(), data, analytics, interactor, navigator,
        AdyenErrorCodeMapper(), servicesErrorCodeMapper, currencyFormatUtils, logger)
  }

  @Provides
  fun providesAdyenPaymentData(fragment: AdyenPaymentFragment): AdyenPaymentData {
    fragment.arguments!!.apply {
      return AdyenPaymentData(RedirectComponent.getReturnUrl(fragment.requireContext()),
          getString(AdyenPaymentFragment.PAYMENT_TYPE_KEY)!!,
          getString(AdyenPaymentFragment.BONUS_KEY)!!,
          getBoolean(AdyenPaymentFragment.PRE_SELECTED_KEY, false),
          getInt(AdyenPaymentFragment.GAMIFICATION_LEVEL, 0),
          getSerializable(AdyenPaymentFragment.TRANSACTION_PAYMENT_DATA) as TransactionPaymentData)
    }
  }

  @Provides
  fun providesIabNavigator(fragment: AdyenPaymentFragment): Navigator {
    return IabNavigator(fragment.requireFragmentManager(), fragment.activity as UriNavigator?,
        fragment.activity as IabView)
  }
}