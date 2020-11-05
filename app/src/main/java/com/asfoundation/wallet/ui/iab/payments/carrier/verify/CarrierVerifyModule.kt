package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingRepository
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

@Module
class CarrierVerifyModule {

  @Provides
  fun providesCarrierVerifyNavigator(fragment: CarrierVerifyFragment): CarrierVerifyNavigator {
    return CarrierVerifyNavigator(
        fragment.requireFragmentManager())
  }

  @Provides
  fun providesCarrierVerifyPhoneData(fragment: CarrierVerifyFragment): CarrierVerifyData {
    fragment.arguments!!.apply {
      return CarrierVerifyData(
          getString(
              CarrierVerifyFragment.DOMAIN_KEY)!!, getString(
          CarrierVerifyFragment.ORIGIN_KEY), getString(
          CarrierVerifyFragment.TRANSACTION_TYPE_KEY) ?: "",
          getString(
              CarrierVerifyFragment.TRANSACTION_DATA_KEY) ?: "", getString(
          CarrierVerifyFragment.CURRENCY_KEY)!!,
          getSerializable(
              CarrierVerifyFragment.FIAT_AMOUNT_KEY) as BigDecimal,
          getSerializable(
              CarrierVerifyFragment.APPC_AMOUNT_KEY) as BigDecimal,
          getSerializable(
              CarrierVerifyFragment.BONUS_AMOUNT_KEY) as BigDecimal, getString(
          CarrierVerifyFragment.SKU_DESCRIPTION)!!)
    }
  }

  @Provides
  fun providesCarrierVerifyPresenter(view: CarrierVerifyView,
                                     data: CarrierVerifyData,
                                     navigator: CarrierVerifyNavigator,
                                     interactor: CarrierInteractor,
                                     applicationInfoLoader: ApplicationInfoLoader): CarrierVerifyPresenter {
    return CarrierVerifyPresenter(
        CompositeDisposable(), view, data, navigator, interactor,
        applicationInfoLoader, AndroidSchedulers.mainThread(), Schedulers.io())
  }

  @Provides
  fun providesCarrierInteractor(repository: CarrierBillingRepository, walletService: WalletService,
                                partnerAddressService: AddressService,
                                inAppPurchaseInteractor: InAppPurchaseInteractor,
                                logger: Logger): CarrierInteractor {
    return CarrierInteractor(repository, walletService, partnerAddressService,
        inAppPurchaseInteractor, logger)
  }
}

@Module
abstract class CarrierVerifyViewModule {
  @Binds
  abstract fun bindCarrierVerifyPhoneView(
      fragment: CarrierVerifyFragment): CarrierVerifyView
}