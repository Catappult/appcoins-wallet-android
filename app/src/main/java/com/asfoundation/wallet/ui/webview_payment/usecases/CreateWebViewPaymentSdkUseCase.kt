package com.asfoundation.wallet.ui.webview_payment.usecases

import com.appcoins.wallet.core.analytics.analytics.IndicativeAnalytics
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.extensions.convertToBase64Url
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCountryCodeUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.tuples.Quintuple
import io.reactivex.Single
import javax.inject.Inject

class CreateWebViewPaymentSdkUseCase @Inject constructor(
  val inAppPurchaseInteractor: InAppPurchaseInteractor,
  val walletService: WalletService,
  val ewtObtainer: EwtAuthenticatorService,
  val getCountryCodeUseCase: GetCountryCodeUseCase,
  val addressService: AddressService,
  val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  val analytics: IndicativeAnalytics,
  val rxSchedulers: RxSchedulers
) {

  val baseWebViewPaymentUrl = HostProperties.WEBVIEW_PAYMENT_URL

  operator fun invoke(
    transaction: TransactionBuilder,
  ): Single<String> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress().subscribeOn(rxSchedulers.io),
      ewtObtainer.getEwtAuthenticationNoBearer()
        .subscribeOn(rxSchedulers.io),
      getCountryCodeUseCase().subscribeOn(rxSchedulers.io),
      addressService.getAttribution(transaction?.domain ?: "").subscribeOn(rxSchedulers.io),
      getCurrentPromoCodeUseCase().subscribeOn(rxSchedulers.io),
    ) { walletModel, ewt, country, oemId, promoCode ->
      Quintuple(walletModel, ewt, country, oemId, promoCode)
    }
      .map { args ->
        val walletModel = args.first
        val ewt = args.second
        val country = args.third
        val oemId = args.fourth.oemId
        val promoCode = args.fifth

        "$baseWebViewPaymentUrl?" +
            "&country=$country" +
            "&address=${walletModel.address}" +
            "&signature=${walletModel.signedAddress}" +
            "&payment_channel=wallet_app" +
            "&token=${ewt}" +
            "&origin=BDS" +
            "&product=${transaction.skuId ?: ""}" +
            "&domain=${transaction.domain ?: ""}" +
            "&type=${transaction.type ?: ""}" +
            "&oem_id=${oemId ?: ""}" +
            "&reference=${
              (transaction.orderReference ?: "").convertToBase64Url()
            }" +
            "&promo_code=${promoCode.code ?: ""}" +
            "&guest_id=${transaction.guestId ?: ""}" +
            "&metadata=${
              (transaction.payload ?: "").convertToBase64Url()
            }" +
            "&period=${transaction.subscriptionPeriod ?: ""}" +
            "&trial_period=${transaction.trialPeriod ?: ""}" +
            "&user_props=${analytics.getIndicativeSuperProperties().convertToBase64Url()}"
      }
  }

}
