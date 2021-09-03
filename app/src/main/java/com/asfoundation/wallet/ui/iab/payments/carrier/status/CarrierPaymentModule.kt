package com.asfoundation.wallet.ui.iab.payments.carrier.status

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

@Module
class CarrierPaymentModule {
  @Provides
  fun providesCarrierPaymentNavigator(fragment: CarrierPaymentFragment,
                                      uriNavigator: UriNavigator): CarrierPaymentNavigator {
    return CarrierPaymentNavigator(fragment.requireFragmentManager(), uriNavigator,
        fragment.activity as IabActivity)
  }

  @Provides
  fun providesCarrierPaymentStatusData(fragment: CarrierPaymentFragment): CarrierPaymentData {
    fragment.arguments!!.apply {
      return CarrierPaymentData(getString(CarrierPaymentFragment.DOMAIN_KEY)!!,
          getString(CarrierPaymentFragment.TRANSACTION_DATA_KEY)!!,
          getString(CarrierPaymentFragment.TRANSACTION_TYPE_KEY)!!,
          getString(CarrierPaymentFragment.SKU_ID_KEY),
          getString(CarrierPaymentFragment.PAYMENT_URL)!!,
          getString(CarrierPaymentFragment.CURRENCY_KEY)!!,
          getSerializable(CarrierPaymentFragment.APPC_AMOUNT_KEY) as BigDecimal,
          getSerializable(CarrierPaymentFragment.BONUS_AMOUNT_KEY) as BigDecimal?,
          getString(CarrierPaymentFragment.PHONE_NUMBER_KEY)!!)
    }
  }

  @Provides
  fun providesCarrierPaymentStatusPresenter(fragment: CarrierPaymentFragment,
                                            data: CarrierPaymentData,
                                            navigator: CarrierPaymentNavigator,
                                            carrierInteractor: CarrierInteractor,
                                            billingAnalytics: BillingAnalytics,
                                            logger: Logger): CarrierPaymentPresenter {
    return CarrierPaymentPresenter(
        CompositeDisposable(), fragment as CarrierPaymentView, data, navigator, carrierInteractor,
        billingAnalytics, logger, AndroidSchedulers.mainThread(), Schedulers.io())
  }

  @Provides
  fun providesUriNavigator(fragment: CarrierPaymentFragment): UriNavigator {
    return fragment.activity as UriNavigator
  }
}