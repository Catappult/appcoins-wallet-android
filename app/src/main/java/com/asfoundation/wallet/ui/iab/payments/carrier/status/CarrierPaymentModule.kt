package com.asfoundation.wallet.ui.iab.payments.carrier.status

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.core.utils.jvm_common.Logger
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
class CarrierPaymentModule {

  @Provides
  fun providesCarrierPaymentStatusData(fragment: Fragment) =
    fragment.requireArguments().run {
      CarrierPaymentData(
        domain = getString(CarrierPaymentFragment.DOMAIN_KEY)!!,
        transactionData = getString(CarrierPaymentFragment.TRANSACTION_DATA_KEY)!!,
        transactionType = getString(CarrierPaymentFragment.TRANSACTION_TYPE_KEY)!!,
        skuId = getString(CarrierPaymentFragment.SKU_ID_KEY),
        paymentUrl = getString(CarrierPaymentFragment.PAYMENT_URL)!!,
        currency = getString(CarrierPaymentFragment.CURRENCY_KEY)!!,
        appcAmount = getSerializableExtra<BigDecimal>(CarrierPaymentFragment.APPC_AMOUNT_KEY)!!,
        bonusAmount = getSerializableExtra<BigDecimal>(CarrierPaymentFragment.BONUS_AMOUNT_KEY),
        phoneNumber = getString(CarrierPaymentFragment.PHONE_NUMBER_KEY)!!
      )
    }

  @Provides
  fun providesCarrierPaymentStatusPresenter(
    fragment: Fragment,
    data: CarrierPaymentData,
    navigator: CarrierPaymentNavigator,
    carrierInteractor: CarrierInteractor,
    billingAnalytics: BillingAnalytics,
    logger: Logger
  ) = CarrierPaymentPresenter(
    disposables = CompositeDisposable(),
    view = fragment as CarrierPaymentView,
    data = data,
    navigator = navigator,
    carrierInteractor = carrierInteractor,
    billingAnalytics = billingAnalytics,
    logger = logger,
    viewScheduler = AndroidSchedulers.mainThread(),
    ioScheduler = Schedulers.io()
  )
}