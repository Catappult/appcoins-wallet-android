package com.asfoundation.wallet.wallet_verification.intro

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.adyen.TransactionResponse
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentPresenter
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class WalletVerificationIntroPresenter(private val view: WalletVerificationIntroView,
                                       private val disposable: CompositeDisposable,
                                       private val navigator: WalletVerificationIntroNavigator,
                                       private val logger: Logger,
                                       private val viewScheduler: Scheduler,
                                       private val ioScheduler: Scheduler,
                                       private val interactor: WalletVerificationIntroInteractor,
                                       private val adyenErrorCodeMapper: AdyenErrorCodeMapper) {

  companion object {

    private val TAG = WalletVerificationIntroPresenter::class.java.name
  }

  fun present(savedInstanceState: Bundle?) {
    loadModel(savedInstanceState)
    handleCancelClicks()
    handleForgetCardClick()
  }

  private fun loadModel(savedInstanceState: Bundle?) {
    disposable.add(
        interactor.loadVerificationIntroModel()
            .subscribeOn(ioScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.finishCardConfiguration(it.paymentInfoModel.paymentMethodInfo!!,
                  it.paymentInfoModel.isStored, false, savedInstanceState)
              view.updateUi(it)
              view.hideLoading()
              view.unlockRotation()
              handleSubmitClicks(it.verificationInfoModel.value, it.verificationInfoModel.currency)
            }
            .doOnSubscribe {
              view.lockRotation()
              view.showLoading()
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleCancelClicks() {
    disposable.add(
        view.getCancelClicks()
            .doOnNext { view.cancel() }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleSubmitClicks(priceAmount: String, priceCurrency: String) {
    disposable.add(
        view.getSubmitClicks()
            .flatMapSingle {
              view.retrievePaymentData()
                  .firstOrError()
            }
            .observeOn(viewScheduler)
            .doOnNext {
              view.showLoading()
              view.hideKeyboard()
              view.lockRotation()
            }
            .flatMapSingle { adyenCard ->
              interactor.makePayment(adyenCard.cardPaymentMethod, adyenCard.shouldStoreCard, "")
            }
            .observeOn(ioScheduler)
            .flatMapCompletable {
              handlePaymentResult(it, priceAmount.toBigDecimal(), priceCurrency)
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handlePaymentResult(paymentModel: PaymentModel, priceAmount: BigDecimal? = null,
                                  priceCurrency: String? = null): Completable {
    return when {
      paymentModel.resultCode.equals("AUTHORISED", true) -> {
        interactor.getAuthorisedTransaction(paymentModel.uid)
            .subscribeOn(ioScheduler)
            .observeOn(viewScheduler)
            .flatMapCompletable {
              when {
                it.status == TransactionResponse.Status.COMPLETED -> {
                  Completable.complete()
                      .observeOn(viewScheduler)
                      .andThen(handleSuccessTransaction())
                }
                isPaymentFailed(it.status) -> {
                  Completable.fromAction { handleErrors(it.error) }
                      .subscribeOn(viewScheduler)
                }
                else -> {
                  Completable.fromAction { handleErrors(it.error) }
                }
              }
            }
      }
      paymentModel.status == TransactionResponse.Status.PENDING_USER_PAYMENT && paymentModel.action != null -> {
        Completable.fromAction {
          view.showLoading()
          view.lockRotation()
          handleAdyenAction(paymentModel)
        }
      }
      paymentModel.refusalReason != null -> Completable.fromAction {
        paymentModel.refusalCode?.let { code ->
          when (code) {
            AdyenErrorCodeMapper.CVC_DECLINED -> view.showCvvError()
            else -> view.showSpecificError(adyenErrorCodeMapper.map(code))
          }
        }
      }
      paymentModel.error.hasError -> Completable.fromAction {
        if (isBillingAddressError(paymentModel.error, priceAmount, priceCurrency)) {
          view.showBillingAddress(priceAmount!!, priceCurrency!!)
        } else {
          handleErrors(paymentModel.error)
        }
      }
      paymentModel.status == TransactionResponse.Status.CANCELED -> Completable.fromAction { view.cancel() }
      else -> Completable.fromAction {
        view.showGenericError()
      }
    }
  }

  private fun handleSuccessTransaction(): Completable {
    return Completable.fromAction { navigator.navigateToCodeView() }
  }

  private fun handleAdyenAction(paymentModel: PaymentModel) {
    if (paymentModel.action != null) {
      val type = paymentModel.action?.type
      if (type == AdyenResponseMapper.REDIRECT) {
        cachedPaymentData = paymentModel.paymentData
        cachedUid = paymentModel.uid
        navigator.navigateToUriForResult(paymentModel.redirectUrl)
        waitingResult = true
      } else if (type == AdyenResponseMapper.THREEDS2FINGERPRINT || type == AdyenResponseMapper.THREEDS2CHALLENGE) {
        cachedUid = paymentModel.uid
        view.handle3DSAction(paymentModel.action!!)
        waitingResult = true
      } else {
        logger.log(AdyenPaymentPresenter.TAG, "Unknown adyen action: $type")
        view.showGenericError()
      }
    }
  }

  private fun isBillingAddressError(error: Error,
                                    priceAmount: BigDecimal?,
                                    priceCurrency: String?): Boolean {
    return error.code != null
        && error.code == 400
        && error.message?.contains("payment.billing_address") == true
        && priceAmount != null
        && priceCurrency != null
  }

  private fun isPaymentFailed(status: TransactionResponse.Status): Boolean {
    return status == TransactionResponse.Status.FAILED || status == TransactionResponse.Status.CANCELED || status == TransactionResponse.Status.INVALID_TRANSACTION
  }

  private fun handleErrors(error: Error) {
    when {
      error.isNetworkError -> view.showNetworkError()
      error.code != null -> {
        val resId = servicesErrorCodeMapper.mapError(error.code!!)
        if (error.code == 403) view.showGenericError()
        else view.showSpecificError(resId)
      }
      else -> view.showGenericError()
    }
  }

  private fun handleForgetCardClick() {
    disposable.add(view.forgetCardClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showLoading() }
        .observeOn(ioScheduler)
        .flatMapSingle { interactor.disablePayments() }
        .observeOn(viewScheduler)
        .doOnNext { success -> if (!success) view.showGenericError() }
        .filter { it }
        .observeOn(ioScheduler)
        .flatMapSingle {
          interactor.loadVerificationIntroModel()
              .doOnSuccess {
                view.updateUi(it)
                view.finishCardConfiguration(it.paymentInfoModel.paymentMethodInfo!!,
                    it.paymentInfoModel.isStored, false, null)
              }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }))
  }

  fun stop() = disposable.clear()
}
