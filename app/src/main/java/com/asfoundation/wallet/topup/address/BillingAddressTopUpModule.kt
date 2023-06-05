package com.asfoundation.wallet.topup.address

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpPaymentData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class BillingAddressTopUpModule {

  @Provides
  fun providesBillingAddressTopUpData(
      fragment: Fragment): BillingAddressTopUpData {
    fragment.requireArguments()
        .apply {
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
  fun providesBillingAddressTopUpPresenter(fragment: Fragment,
                                           data: BillingAddressTopUpData,
                                           navigator: BillingAddressTopUpNavigator,
                                           billingAddressRepository: BillingAddressRepository,
                                           topUpAnalytics: TopUpAnalytics): BillingAddressTopUpPresenter {
    return BillingAddressTopUpPresenter(fragment as BillingAddressTopUpView, data,
        CompositeDisposable(),
        AndroidSchedulers.mainThread(), navigator, billingAddressRepository, topUpAnalytics)
  }
}