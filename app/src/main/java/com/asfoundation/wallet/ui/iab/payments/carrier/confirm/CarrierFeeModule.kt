package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoProvider
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

@Module
class CarrierFeeModule {

  @Provides
  fun providesCarrierFeeNavigator(fragment: CarrierFeeFragment): CarrierFeeNavigator {
    return CarrierFeeNavigator(fragment.activity as IabActivity, fragment.requireFragmentManager())
  }

  @Provides
  fun providesCarrierFeePhoneData(fragment: CarrierFeeFragment): CarrierFeeData {
    fragment.arguments!!.apply {
      return CarrierFeeData(getString(CarrierFeeFragment.UID_KEY)!!,
          getString(CarrierFeeFragment.DOMAIN_KEY)!!,
          getString(CarrierFeeFragment.TRANSACTION_DATA_KEY)!!,
          getString(CarrierFeeFragment.TRANSACTION_TYPE_KEY)!!,
          getString(CarrierFeeFragment.SKU_DESCRIPTION)!!,
          getString(CarrierFeeFragment.SKU_ID),
          getString(CarrierFeeFragment.PAYMENT_URL_KEY)!!,
          getString(CarrierFeeFragment.CURRENCY_KEY)!!,
          getSerializable(CarrierFeeFragment.FIAT_AMOUNT_KEY) as BigDecimal,
          getSerializable(CarrierFeeFragment.APPC_AMOUNT_KEY) as BigDecimal,
          getSerializable(CarrierFeeFragment.BONUS_AMOUNT_KEY) as BigDecimal?,
          getSerializable(CarrierFeeFragment.FEE_FIAT_AMOUNT) as BigDecimal,
          getString(CarrierFeeFragment.CARRIER_NAME)!!,
          getString(CarrierFeeFragment.CARRIER_IMAGE)!!)
    }
  }

  @Provides
  fun providesCarrierFeePresenter(fragment: CarrierFeeFragment,
                                  data: CarrierFeeData,
                                  navigator: CarrierFeeNavigator,
                                  billingAnalytics: BillingAnalytics,
                                  appInfoProvider: ApplicationInfoProvider): CarrierFeePresenter {
    return CarrierFeePresenter(CompositeDisposable(), fragment as CarrierFeeView, data,
        navigator, billingAnalytics, appInfoProvider, AndroidSchedulers.mainThread())
  }
}