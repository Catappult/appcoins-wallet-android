package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.applicationinfo.ApplicationInfoProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

@InstallIn(FragmentComponent::class)
@Module
class CarrierFeeModule {

  @Provides
  fun providesCarrierFeePhoneData(fragment: Fragment): CarrierFeeData {
    fragment.requireArguments()
        .apply {
          return CarrierFeeData(getString(CarrierFeeFragment.UID_KEY)!!,
              getString(CarrierFeeFragment.DOMAIN_KEY)!!,
              getString(CarrierFeeFragment.TRANSACTION_DATA_KEY)!!,
              getString(CarrierFeeFragment.TRANSACTION_TYPE_KEY)!!,
              getString(CarrierFeeFragment.SKU_DESCRIPTION_KEY)!!,
              getString(CarrierFeeFragment.SKU_ID_KEY),
              getString(CarrierFeeFragment.PAYMENT_URL_KEY)!!,
              getString(CarrierFeeFragment.CURRENCY_KEY)!!,
              getSerializable(CarrierFeeFragment.FIAT_AMOUNT_KEY) as BigDecimal,
              getSerializable(CarrierFeeFragment.APPC_AMOUNT_KEY) as BigDecimal,
              getSerializable(CarrierFeeFragment.BONUS_AMOUNT_KEY) as BigDecimal?,
              getSerializable(CarrierFeeFragment.FEE_FIAT_AMOUNT_KEY) as BigDecimal,
              getString(CarrierFeeFragment.CARRIER_NAME_KEY)!!,
              getString(CarrierFeeFragment.CARRIER_IMAGE_KEY)!!,
              getString(CarrierFeeFragment.PHONE_NUMBER_KEY)!!)
        }
  }

  @Provides
  fun providesCarrierFeePresenter(fragment: Fragment,
                                  data: CarrierFeeData,
                                  navigator: CarrierFeeNavigator,
                                  billingAnalytics: BillingAnalytics,
                                  appInfoProvider: ApplicationInfoProvider
  ): CarrierFeePresenter {
    return CarrierFeePresenter(CompositeDisposable(), fragment as CarrierFeeView, data,
        navigator, billingAnalytics, appInfoProvider, AndroidSchedulers.mainThread())
  }
}