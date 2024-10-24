package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.applicationinfo.ApplicationInfoProvider
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.ui.common.StringProvider
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
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
  fun providesCarrierVerifyPhoneData(fragment: Fragment) =
    fragment.requireArguments().run {
      CarrierVerifyData(
        getBoolean(CarrierVerifyFragment.PRE_SELECTED_KEY),
        getString(CarrierVerifyFragment.DOMAIN_KEY)!!,
        getString(CarrierVerifyFragment.ORIGIN_KEY),
        getString(CarrierVerifyFragment.TRANSACTION_TYPE_KEY) ?: "",
        getString(CarrierVerifyFragment.TRANSACTION_DATA_KEY) ?: "",
        getString(CarrierVerifyFragment.CURRENCY_KEY)!!,
        getSerializableExtra<BigDecimal>(CarrierVerifyFragment.FIAT_AMOUNT_KEY)!!,
        getSerializableExtra<BigDecimal>(CarrierVerifyFragment.APPC_AMOUNT_KEY)!!,
        getSerializableExtra<BigDecimal>(CarrierVerifyFragment.BONUS_AMOUNT_KEY),
        getString(CarrierVerifyFragment.SKU_DESCRIPTION)!!,
        getString(CarrierVerifyFragment.SKU_ID)
      )
    }

  @Provides
  fun providesCarrierVerifyPresenter(
    fragment: Fragment,
    data: CarrierVerifyData,
    navigator: CarrierVerifyNavigator,
    interactor: CarrierInteractor,
    billingAnalytics: BillingAnalytics,
    stringProvider: StringProvider,
    applicationInfoProvider: ApplicationInfoProvider,
    logger: Logger
  ): CarrierVerifyPresenter {
    return CarrierVerifyPresenter(
      CompositeDisposable(), fragment as CarrierVerifyView, data,
      navigator, interactor, billingAnalytics, applicationInfoProvider, stringProvider,
      CurrencyFormatUtils(), logger, Schedulers.io(), AndroidSchedulers.mainThread()
    )
  }
}