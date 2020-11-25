package com.asfoundation.wallet.verification.intro

import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.adyen.VerificationInfoResponse
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.support.SupportInteractor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class WalletVerificationIntroInteractor(
    private val adyenPaymentRepository: AdyenPaymentRepository,
    private val adyenPaymentInteractor: AdyenPaymentInteractor,
    private val walletService: WalletService,
    private val supportInteractor: SupportInteractor
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
                  returnUrl: String): Single<PaymentModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          adyenPaymentRepository.makeVerificationPayment(adyenPaymentMethod, shouldStoreMethod,
              returnUrl, it.address, it.signedAddress)
        }
  }

  fun getAuthorisedTransaction(uid: String): Observable<PaymentModel> {
    return adyenPaymentInteractor.getAuthorisedTransaction(uid)
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
          adyenPaymentRepository.getVerificationInfo(it.address, it.signedAddress)
              .map { info -> mapToVerificationInfoModel(info) }
        }
  }

  private fun mapToVerificationInfoModel(
      response: VerificationInfoResponse): VerificationInfoModel {
    return VerificationInfoModel(response.currency, response.symbol, response.value,
        response.digits, response.format, response.period)
  }

  fun showSupport(): Completable {
    return Completable.fromAction {
      supportInteractor.displayChatScreen()
    }

  }
}