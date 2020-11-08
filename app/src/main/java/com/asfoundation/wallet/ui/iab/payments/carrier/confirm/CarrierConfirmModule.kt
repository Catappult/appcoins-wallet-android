package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

@Module
class CarrierConfirmModule {

  @Provides
  fun providesCarrierConfirmNavigator(fragment: CarrierConfirmFragment,
                                      uriNavigator: UriNavigator): CarrierConfirmNavigator {
    return CarrierConfirmNavigator(fragment.requireFragmentManager(), uriNavigator)
  }

  @Provides
  fun providesCarrierConfirmPhoneData(fragment: CarrierConfirmFragment): CarrierConfirmData {
    fragment.arguments!!.apply {
      return CarrierConfirmData(
          getString(
              CarrierConfirmFragment.DOMAIN_KEY)!!, getString(
          CarrierConfirmFragment.SKU_DESCRIPTION)!!,
          getString(
              CarrierConfirmFragment.PAYMENT_URL_KEY)!!, getString(
          CarrierConfirmFragment.CURRENCY_KEY)!!,
          getSerializable(
              CarrierConfirmFragment.FIAT_AMOUNT_KEY) as BigDecimal,
          getSerializable(
              CarrierConfirmFragment.APPC_AMOUNT_KEY) as BigDecimal,
          getSerializable(
              CarrierConfirmFragment.BONUS_AMOUNT_KEY) as BigDecimal,
          getSerializable(
              CarrierConfirmFragment.FEE_FIAT_AMOUNT) as BigDecimal,
          getString(
              CarrierConfirmFragment.CARRIER_NAME)!!, getString(
          CarrierConfirmFragment.CARRIER_IMAGE)!!)
    }
  }

  @Provides
  fun providesCarrierConfirmPresenter(view: CarrierConfirmView,
                                      data: CarrierConfirmData,
                                      navigator: CarrierConfirmNavigator,
                                      appInfoLoader: ApplicationInfoLoader): CarrierConfirmPresenter {
    return CarrierConfirmPresenter(
        CompositeDisposable(), view, data, navigator, appInfoLoader, AndroidSchedulers.mainThread(),
        Schedulers.io())
  }

  @Provides
  fun providesUriNavigator(fragment: CarrierConfirmFragment): UriNavigator {
    return fragment.activity as UriNavigator
  }
}

@Module
abstract class CarrierConfirmViewModule {
  @Binds
  abstract fun bindCarrierConfirmView(
      fragment: CarrierConfirmFragment): CarrierConfirmView
}