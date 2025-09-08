package com.asfoundation.wallet.ui.webview_payment.usecases

import com.appcoins.wallet.core.analytics.analytics.IndicativeAnalytics
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.extensions.convertToBase64Url
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetCachedCurrencyUseCase
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCountryCodeUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.webview_login.usecases.GenerateWebLoginUrlUseCase
import com.asfoundation.wallet.util.tuples.Sextuple
import io.reactivex.Single
import javax.inject.Inject

class CreateWebViewPaymentOspUseCase @Inject constructor(
  val inAppPurchaseInteractor: InAppPurchaseInteractor,
  val walletService: WalletService,
  val ewtObtainer: EwtAuthenticatorService,
  val getCountryCodeUseCase: GetCountryCodeUseCase,
  val addressService: AddressService,
  val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  val analytics: IndicativeAnalytics,
  val getCachedCurrencyUseCase: GetCachedCurrencyUseCase,
  val generateWebLoginUrlUseCase: GenerateWebLoginUrlUseCase,
  val getEncryptedPrivateKeyUseCase: GetEncryptedPrivateKeyUseCase,
  val rxSchedulers: RxSchedulers
) {

  val baseWebViewPaymentUrl = HostProperties.WEBVIEW_PAYMENT_URL

  operator fun invoke(
    transaction: TransactionBuilder,
    appVersion: String?
  ): Single<String> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress().subscribeOn(rxSchedulers.io),
      ewtObtainer.getEwtAuthenticationNoBearer().subscribeOn(rxSchedulers.io),
      getCountryCodeUseCase().subscribeOn(rxSchedulers.io),
      addressService.getAttribution(transaction?.domain ?: "").subscribeOn(rxSchedulers.io),
      getCurrentPromoCodeUseCase().subscribeOn(rxSchedulers.io),
      getEncryptedPrivateKeyUseCase().subscribeOn(rxSchedulers.io),
    ) { walletModel, ewt, country, oemId, promoCode, encrypt ->
      Sextuple(walletModel, ewt, country, oemId, promoCode, encrypt)
    }
      .map { args ->
        val walletModel = args.first
        val ewt = args.second
        val country = args.third
        val oemId = args.fourth.oemId
        val promoCode = args.fifth
        val encrypt = args.sixth

        "$baseWebViewPaymentUrl?" +
            "referrer_url=${
              transaction.referrerUrl.convertToBase64Url()
            }" +
            "&country=$country" +
            "&address=${walletModel.address}" +
            "&payment_channel=${generateWebLoginUrlUseCase.mapPaymentChannel()}" +
            "&token=${ewt}" +
            "&origin=BDS" +
            "&product=${transaction.skuId ?: ""}" +
            "&domain=${transaction.domain ?: ""}" +
            "&type=${transaction.type ?: ""}" +
            "&oem_id=${oemId ?: ""}" +
            "&reference=${transaction.orderReference ?: ""}" +
            "&promo_code=${promoCode.code ?: ""}" +
            "&version=${appVersion ?: ""}" +
            "&currency=".plus(if (getCachedCurrencyUseCase().equals("null")) "" else getCachedCurrencyUseCase()) +
            "&user_props=${analytics.getIndicativeSuperProperties().convertToBase64Url()}" +
            if (generateWebLoginUrlUseCase.isCloudGaming())
              "&user=${encrypt}"
            else ""
      }
  }

}
