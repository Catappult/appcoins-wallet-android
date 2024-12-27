package com.asfoundation.wallet.ui.webview_payment.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.extensions.convertToBase64Url
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCountryCodeUseCase
import com.asfoundation.wallet.entity.TransactionBuilder

import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.tuples.Quintuple
import io.reactivex.Single
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class CreateWebViewPaymentSdkUseCase @Inject constructor(
  val inAppPurchaseInteractor: InAppPurchaseInteractor,
  val walletService: WalletService,
  val ewtObtainer: EwtAuthenticatorService,
  val getCountryCodeUseCase: GetCountryCodeUseCase,
  val addressService: AddressService,
  val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  val rxSchedulers: RxSchedulers
) {

  val baseWebViewPaymentUrl = "https://wallet.dev.appcoins.io/iap"  //TODO from buildConfig

  operator fun invoke(
    transaction: TransactionBuilder,
  ): Single<String> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress().subscribeOn(rxSchedulers.io),
      ewtObtainer.getEwtAuthenticationNoBearer()
        .subscribeOn(rxSchedulers.io), // TODO confirmar wallet usada
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
            "&product=${transaction.skuId}" +
            "&domain=${transaction.domain}" +
            "&type=${transaction.type}" +
            "&oem_id=${oemId ?: ""}" +
            "&reference=${
              transaction.referrerUrl.convertToBase64Url()
            }" +
            "&promo_code=${promoCode.code ?: ""}" +
            "&guest_id=${transaction.guestId ?: ""}" +
            "&metadata=${
              URLEncoder.encode(
                transaction.payload ?: "",
                StandardCharsets.UTF_8.toString()
              )
            }" +
            "&period=${transaction.subscriptionPeriod ?: ""}" +
            "&trial_period=${transaction.trialPeriod ?: ""}"
      }
  }

}
