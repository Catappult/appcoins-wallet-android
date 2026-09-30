package com.asfoundation.wallet.ui.webview_payment.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.webview_payment.repository.PayFlowRepository
import com.asfoundation.wallet.ui.webview_payment.repository.PayFlowResult
import io.reactivex.Single
import javax.inject.Inject

class IsWebViewPaymentFlowUseCase @Inject constructor(
  val partnerAddressService: PartnerAddressService,
  val rxSchedulers: RxSchedulers,
  val payFlowRepository: PayFlowRepository,
) {

  /**
   * Returns true when the payment should go through the web checkout. If the pay flow can't be
   * obtained (timeout, no connectivity, server error), defaults to the web checkout, since it is
   * served from a different host than the pay flow API.
   */
  operator fun invoke(
    transaction: TransactionBuilder,
    appVersionCode: Int?
  ): Single<Boolean> {
    return partnerAddressService.getAttribution(transaction.domain)
      .flatMap { attributionEntity ->
        payFlowRepository.getPayFlow(
          packageName = transaction.domain,
          oemid = attributionEntity.oemId,
          appVersionCode = appVersionCode
        )
      }
      .map { result ->
        when (result) {
          is PayFlowResult.Success -> result.response.paymentMethods?.walletWebViewPayment != null
          is PayFlowResult.Unreachable -> true
        }
      }
  }

}
