package com.asfoundation.wallet.topup.address

import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpPaymentData
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@Module
class BillingAddressTopUpModule {

  @Provides
  fun providesBillingAddressTopUpData(
      fragment: BillingAddressTopUpFragment): BillingAddressTopUpData {
    fragment.arguments!!.apply {
      return BillingAddressTopUpData(
          getSerializable(BillingAddressTopUpFragment.PAYMENT_DATA) as TopUpPaymentData,
          getString(BillingAddressTopUpFragment.FIAT_AMOUNT_KEY)!!,
          getString(BillingAddressTopUpFragment.FIAT_CURRENCY_KEY)!!,
          getBoolean(BillingAddressTopUpFragment.STORE_CARD_KEY),
          getBoolean(BillingAddressTopUpFragment.IS_STORED_KEY)
      )
    }
  }

  @Provides
  fun providesBillingAddressTopUpPresenter(fragment: BillingAddressTopUpFragment,
                                           data: BillingAddressTopUpData,
                                           navigator: BillingAddressTopUpNavigator,
                                           billingAddressRepository: BillingAddressRepository,
                                           topUpAnalytics: TopUpAnalytics): BillingAddressTopUpPresenter {
    return BillingAddressTopUpPresenter(fragment, data, CompositeDisposable(),
        AndroidSchedulers.mainThread(), navigator, billingAddressRepository, topUpAnalytics)
  }

  @Provides
  fun providesBillingAddressTopUpNavigator(
      fragment: BillingAddressTopUpFragment): BillingAddressTopUpNavigator {
    return BillingAddressTopUpNavigator(fragment.requireFragmentManager(),
        fragment.context as TopUpActivityView)
  }
}