package com.asfoundation.wallet.ui.webview_payment.usecases

import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.network.microservices.model.PayFlowResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.webview_payment.repository.PayFlowRepository
import io.reactivex.Single
import javax.inject.Inject

class IsWebViewPaymentFlowUseCase @Inject constructor(
  val partnerAddressService: PartnerAddressService,
  val rxSchedulers: RxSchedulers,
  val payFlowRepository: PayFlowRepository,
) {

  operator fun invoke(
    transaction: TransactionBuilder,
    appVersionCode: Int?
  ): Single<PayFlowResponse> {
    return partnerAddressService.getAttribution(transaction.domain)
      .flatMap { attributionEntity ->
        payFlowRepository.getPayFlow(
          packageName = transaction.domain,
          oemid = attributionEntity.oemId,
          appVersionCode = appVersionCode
        )
      }
  }

}
