package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.ui.iab.IabActivity
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
  fun providesCarrierConfirmNavigator(fragment: CarrierConfirmFragment): CarrierConfirmNavigator {
    return CarrierConfirmNavigator(fragment.activity as IabActivity,
        fragment.requireFragmentManager())
  }

  @Provides
  fun providesCarrierConfirmPhoneData(fragment: CarrierConfirmFragment): CarrierConfirmData {
    fragment.arguments!!.apply {
      return CarrierConfirmData(getString(CarrierConfirmFragment.UID_KEY)!!,
          getString(CarrierConfirmFragment.DOMAIN_KEY)!!,
          getString(CarrierConfirmFragment.TRANSACTION_DATA_KEY)!!,
          getString(CarrierConfirmFragment.TRANSACTION_TYPE_KEY)!!,
          getString(CarrierConfirmFragment.SKU_DESCRIPTION)!!,
          getString(CarrierConfirmFragment.SKU_ID),
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
                                      billingAnalytics: BillingAnalytics,
                                      appInfoLoader: ApplicationInfoLoader): CarrierConfirmPresenter {
    return CarrierConfirmPresenter(CompositeDisposable(), fragment as CarrierConfirmView, data,
        navigator, billingAnalytics, appInfoLoader, AndroidSchedulers.mainThread(), Schedulers.io())
  }
}