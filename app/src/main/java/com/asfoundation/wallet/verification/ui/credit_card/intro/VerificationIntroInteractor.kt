package com.asfoundation.wallet.verification.ui.credit_card.intro

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.appcoins.wallet.core.network.microservices.model.VerificationInfoResponse
import com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.wallet.appcoins.feature.support.data.SupportInteractor
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class VerificationIntroInteractor @Inject constructor(
        private val brokerVerificationRepository: BrokerVerificationRepository,
        private val adyenPaymentInteractor: AdyenPaymentInteractor,
        private val walletService: WalletService,
        private val supportInteractor: SupportInteractor,
        private val walletVerificationInteractor: WalletVerificationInteractor
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

  fun makePayment(adyenPaymentMethod: ModelObject, shouldStoreMethod: Boolean,
                  returnUrl: String): Single<VerificationPaymentModel> {
    return walletVerificationInteractor.makeVerificationPayment(
        WalletVerificationInteractor.VerificationType.CREDIT_CARD, adyenPaymentMethod,
        shouldStoreMethod, returnUrl)
  }

  private fun mapToVerificationIntroModel(infoModel: VerificationInfoModel,
                                          paymentInfoModel: PaymentInfoModel): VerificationIntroModel {
    return VerificationIntroModel(infoModel, paymentInfoModel)
  }

  private fun loadPaymentMethodInfo(currency: String, amount: String): Single<PaymentInfoModel> {
    return adyenPaymentInteractor.loadPaymentInfo(PAYMENT_TYPE, amount, currency)
  }

  private fun getVerificationInfo(): Single<VerificationInfoModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          brokerVerificationRepository.getVerificationInfo(it.address, it.signedAddress)
              .map { info -> mapToVerificationInfoModel(info) }
        }
  }

  private fun mapToVerificationInfoModel(
      response: VerificationInfoResponse
  ): VerificationInfoModel {
    return VerificationInfoModel(response.currency, response.symbol, response.value,
        response.digits, response.format, response.period)
  }

  fun showSupport(): Completable {
    return Completable.fromAction {
      supportInteractor.displayChatScreen()
    }
  }
}