package com.asfoundation.wallet.ui.iab.localpayments

import com.appcoins.wallet.appcoins.rewards.ErrorMapper
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.navigator.UriNavigator
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

@Module
class LocalPaymentModule {

  @Provides
  fun providesLocalPaymentPresenter(fragment: LocalPaymentFragment, data: LocalPaymentData,
                                    interactor: LocalPaymentInteractor,
                                    navigator: LocalPaymentNavigator,
                                    analytics: LocalPaymentAnalytics,
                                    logger: Logger,
                                    errorMapper: ErrorMapper): LocalPaymentPresenter {
    return LocalPaymentPresenter(fragment as LocalPaymentView, data, interactor, navigator,
        analytics, AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(),
        fragment.context, logger, errorMapper)
  }

  @Provides
  fun providesLocalPaymentData(fragment: LocalPaymentFragment): LocalPaymentData {
    fragment.arguments!!.apply {
      return LocalPaymentData(getString(LocalPaymentFragment.DOMAIN_KEY)!!,
          getString(LocalPaymentFragment.SKU_ID_KEY),
          getString(LocalPaymentFragment.ORIGINAL_AMOUNT_KEY),
          getString(LocalPaymentFragment.CURRENCY_KEY),
          getString(LocalPaymentFragment.BONUS_KEY),
          getString(LocalPaymentFragment.PAYMENT_KEY)!!,
          getString(LocalPaymentFragment.DEV_ADDRESS_KEY)!!,
          getString(LocalPaymentFragment.TYPE_KEY)!!,
          getSerializable(LocalPaymentFragment.AMOUNT_KEY) as BigDecimal,
          getString(LocalPaymentFragment.CALLBACK_URL),
          getString(LocalPaymentFragment.ORDER_REFERENCE),
          getString(LocalPaymentFragment.PAYLOAD),
          getString(LocalPaymentFragment.ORIGIN),
          getString(LocalPaymentFragment.PAYMENT_METHOD_URL),
          getString(LocalPaymentFragment.PAYMENT_METHOD_LABEL),
          getBoolean(LocalPaymentFragment.ASYNC),
          getString(LocalPaymentFragment.REFERRER_URL),
          getInt(LocalPaymentFragment.GAMIFICATION_LEVEL))
    }
  }

  @Provides
  fun providesLocalPaymentNavigator(uriNavigator: UriNavigator): LocalPaymentNavigator {
    return LocalPaymentNavigator(uriNavigator)
  }

  @Provides
  fun providesUriNavigator(fragment: LocalPaymentFragment): UriNavigator {
    return fragment.activity as UriNavigator
  }
}