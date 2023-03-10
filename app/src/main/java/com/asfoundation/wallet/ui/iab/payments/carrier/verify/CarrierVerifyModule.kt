package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import androidx.fragment.app.Fragment
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.StringProvider
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

@InstallIn(FragmentComponent::class)
@Module
class CarrierVerifyModule {

  @Provides
  fun providesCarrierVerifyPhoneData(fragment: Fragment): CarrierVerifyData {
    fragment.requireArguments()
        .apply {
          return CarrierVerifyData(getBoolean(CarrierVerifyFragment.PRE_SELECTED_KEY),
              getString(CarrierVerifyFragment.DOMAIN_KEY)!!,
              getString(CarrierVerifyFragment.ORIGIN_KEY),
              getString(CarrierVerifyFragment.TRANSACTION_TYPE_KEY) ?: "",
              getString(CarrierVerifyFragment.TRANSACTION_DATA_KEY) ?: "",
              getString(CarrierVerifyFragment.CURRENCY_KEY)!!,
              getSerializable(CarrierVerifyFragment.FIAT_AMOUNT_KEY) as BigDecimal,
              getSerializable(CarrierVerifyFragment.APPC_AMOUNT_KEY) as BigDecimal,
              getSerializable(CarrierVerifyFragment.BONUS_AMOUNT_KEY) as BigDecimal?,
              getString(CarrierVerifyFragment.SKU_DESCRIPTION)!!,
              getString(CarrierVerifyFragment.SKU_ID))
        }
  }

  @Provides
  fun providesCarrierVerifyPresenter(fragment: Fragment,
                                     data: CarrierVerifyData,
                                     navigator: CarrierVerifyNavigator,
                                     interactor: CarrierInteractor,
                                     billingAnalytics: BillingAnalytics,
                                     stringProvider: StringProvider,
                                     applicationInfoProvider: ApplicationInfoProvider,
                                     logger: Logger): CarrierVerifyPresenter {
    return CarrierVerifyPresenter(CompositeDisposable(), fragment as CarrierVerifyView, data,
        navigator, interactor, billingAnalytics, applicationInfoProvider, stringProvider,
        CurrencyFormatUtils(), logger, Schedulers.io(), AndroidSchedulers.mainThread())
  }
}