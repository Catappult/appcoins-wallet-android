package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

@Module
class CarrierConfirmModule {

  @Provides
  fun providesCarrierConfirmNavigator(fragment: CarrierConfirmFragment,
                                      uriNavigator: UriNavigator,
                                      iabActivity: IabActivity): CarrierConfirmNavigator {
    return CarrierConfirmNavigator(fragment.requireFragmentManager(), uriNavigator, iabActivity)
  }

  @Provides
  fun providesCarrierConfirmPhoneData(fragment: CarrierConfirmFragment): CarrierConfirmData {
    fragment.arguments!!.apply {
      return CarrierConfirmData(
          getString(
              CarrierConfirmFragment.DOMAIN_KEY)!!,
          getString(
              CarrierConfirmFragment.TRANSACTION_DATA_KEY)!!,
          getString(
              CarrierConfirmFragment.TRANSACTION_TYPE_KEY)!!,
          getString(CarrierConfirmFragment.SKU_DESCRIPTION)!!,
          getString(CarrierConfirmFragment.PAYMENT_URL_KEY)!!,
          getString(CarrierConfirmFragment.CURRENCY_KEY)!!,
          getSerializable(CarrierConfirmFragment.FIAT_AMOUNT_KEY) as BigDecimal,
          getSerializable(CarrierConfirmFragment.APPC_AMOUNT_KEY) as BigDecimal,
          getSerializable(CarrierConfirmFragment.BONUS_AMOUNT_KEY) as BigDecimal,
          getSerializable(CarrierConfirmFragment.FEE_FIAT_AMOUNT) as BigDecimal,
          getString(CarrierConfirmFragment.CARRIER_NAME)!!,
          getString(CarrierConfirmFragment.CARRIER_IMAGE)!!)
    }
  }

  @Provides
  fun providesCarrierConfirmPresenter(fragment: CarrierConfirmFragment,
                                      data: CarrierConfirmData,
                                      navigator: CarrierConfirmNavigator,
                                      interactor: CarrierInteractor,
                                      billingAnalytics: BillingAnalytics,
                                      logger: Logger,
                                      appInfoLoader: ApplicationInfoLoader): CarrierConfirmPresenter {
    return CarrierConfirmPresenter(
        CompositeDisposable(), fragment as CarrierConfirmView, data, navigator, interactor,
        billingAnalytics, appInfoLoader, logger, AndroidSchedulers.mainThread(), Schedulers.io())
  }

  @Provides
  fun providesUriNavigator(fragment: CarrierConfirmFragment): UriNavigator {
    return fragment.activity as UriNavigator
  }
}