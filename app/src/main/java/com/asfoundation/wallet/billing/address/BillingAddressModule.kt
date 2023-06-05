package com.asfoundation.wallet.billing.address

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

@InstallIn(FragmentComponent::class)
@Module
class BillingAddressModule {

  @Provides
  fun providesBillingAddressPresenter(fragment: Fragment,
                                      navigator: BillingAddressNavigator,
                                      data: BillingAddressData,
                                      billingAddressRepository: BillingAddressRepository,
                                      billingAnalytics: BillingAnalytics): BillingAddressPresenter {
    return BillingAddressPresenter(fragment as BillingAddressView, data, navigator,
        billingAddressRepository,
        billingAnalytics, CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesBillingAddressData(fragment: Fragment): BillingAddressData {
    fragment.requireArguments()
        .apply {
          return BillingAddressData(
              getString(BillingAddressFragment.SKU_ID)!!,
              getString(BillingAddressFragment.SKU_DESCRIPTION)!!,
              getString(BillingAddressFragment.TRANSACTION_TYPE)!!,
              getString(BillingAddressFragment.DOMAIN_KEY)!!,
              getString(BillingAddressFragment.BONUS_KEY),
              getSerializable(BillingAddressFragment.APPC_AMOUNT_KEY) as BigDecimal,
              getSerializable(BillingAddressFragment.FIAT_AMOUNT_KEY) as BigDecimal,
              getString(BillingAddressFragment.FIAT_CURRENCY_KEY)!!,
              getBoolean(BillingAddressFragment.IS_DONATION_KEY),
              getBoolean(BillingAddressFragment.STORE_CARD_KEY),
              getBoolean(BillingAddressFragment.IS_STORED_KEY))
        }
  }
}