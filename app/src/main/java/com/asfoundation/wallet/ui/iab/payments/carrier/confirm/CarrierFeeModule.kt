package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.applicationinfo.ApplicationInfoProvider
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
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
  fun providesCarrierFeePhoneData(fragment: Fragment) =
    fragment.requireArguments().run {
      CarrierFeeData(
        uid = getString(CarrierFeeFragment.UID_KEY)!!,
        domain = getString(CarrierFeeFragment.DOMAIN_KEY)!!,
        transactionData = getString(CarrierFeeFragment.TRANSACTION_DATA_KEY)!!,
        transactionType = getString(CarrierFeeFragment.TRANSACTION_TYPE_KEY)!!,
        skuDescription = getString(CarrierFeeFragment.SKU_DESCRIPTION_KEY)!!,
        skuId = getString(CarrierFeeFragment.SKU_ID_KEY),
        paymentUrl = getString(CarrierFeeFragment.PAYMENT_URL_KEY)!!,
        currency = getString(CarrierFeeFragment.CURRENCY_KEY)!!,
        fiatAmount = getSerializableExtra<BigDecimal>(CarrierFeeFragment.FIAT_AMOUNT_KEY)!!,
        appcAmount = getSerializableExtra<BigDecimal>(CarrierFeeFragment.APPC_AMOUNT_KEY)!!,
        bonusAmount = getSerializableExtra<BigDecimal>(CarrierFeeFragment.BONUS_AMOUNT_KEY),
        feeFiatAmount = getSerializableExtra<BigDecimal>(CarrierFeeFragment.FEE_FIAT_AMOUNT_KEY)!!,
        carrierName = getString(CarrierFeeFragment.CARRIER_NAME_KEY)!!,
        carrierImage = getString(CarrierFeeFragment.CARRIER_IMAGE_KEY)!!,
        phoneNumber = getString(CarrierFeeFragment.PHONE_NUMBER_KEY)!!
      )
    }

  @Provides
  fun providesCarrierFeePresenter(
    fragment: Fragment,
    data: CarrierFeeData,
    navigator: CarrierFeeNavigator,
    billingAnalytics: BillingAnalytics,
    appInfoProvider: ApplicationInfoProvider
  ) = CarrierFeePresenter(
    disposables = CompositeDisposable(),
    view = fragment as CarrierFeeView,
    data = data,
    navigator = navigator,
    billingAnalytics = billingAnalytics,
    appInfoProvider = appInfoProvider,
    viewScheduler = AndroidSchedulers.mainThread()
  )
}