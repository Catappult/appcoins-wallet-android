package com.asfoundation.wallet.billing.address

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.ui.iab.IabActivity
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

@Module
class BillingAddressModule {

  @Provides
  fun providesBillingAddressNavigator(fragment: BillingAddressFragment): BillingAddressNavigator {
    return BillingAddressNavigator(fragment, fragment.activity as IabActivity)
  }

  @Provides
  fun providesBillingAddressPresenter(fragment: BillingAddressFragment,
                                      navigator: BillingAddressNavigator,
                                      data: BillingAddressData,
                                      billingAnalytics: BillingAnalytics): BillingAddressPresenter {
    return BillingAddressPresenter(fragment, data, navigator, billingAnalytics,
        CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  @Provides
  fun providesBillingAddressData(fragment: BillingAddressFragment): BillingAddressData {
    fragment.arguments!!.apply {
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