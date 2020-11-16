package com.asfoundation.wallet.wallet_verification.intro

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.VerificationInfoResponse
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import io.reactivex.Single

class WalletVerificationIntroInteractor(
    private val adyenPaymentRepository: AdyenPaymentRepository,
    private val adyenPaymentInteractor: AdyenPaymentInteractor
) {

  companion object {
    val PAYMENT_TYPE = AdyenPaymentRepository.Methods.CREDIT_CARD
  }

  fun loadVerificationIntroModel(): Single<VerificationIntroModel> {
    return getVerificationInfo()
        .flatMap {
          loadPaymentMethodInfo(it.currency, it.value)
              .map { paymentInfoModel -> mapToVerificationIntroModel(it, paymentInfoModel) }
        }
  }

  fun disablePayments(): Single<Boolean> {
    return adyenPaymentInteractor.disablePayments()
  }

  private fun mapToVerificationIntroModel(infoModel: VerificationInfoModel,
                                          paymentInfoModel: PaymentInfoModel): VerificationIntroModel {
    return VerificationIntroModel(infoModel, paymentInfoModel)
  }

  fun loadPaymentMethodInfo(currency: String, amount: String): Single<PaymentInfoModel> {
    return adyenPaymentInteractor.loadPaymentInfo(PAYMENT_TYPE, amount, currency)
  }

  private fun getVerificationInfo(): Single<VerificationInfoModel> {
    return adyenPaymentRepository.getVerificationInfo()
        .map { mapToVerificationInfoModel(it) }
  }

  private fun mapToVerificationInfoModel(
      response: VerificationInfoResponse): VerificationInfoModel {
    return VerificationInfoModel(response.currency, response.value, response.digits,
        response.format, response.period)
  }

}