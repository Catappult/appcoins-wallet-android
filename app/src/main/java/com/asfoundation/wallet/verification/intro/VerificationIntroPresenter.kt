package com.asfoundation.wallet.verification.intro

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.verification.VerificationAnalytics
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class VerificationIntroPresenter(private val view: VerificationIntroView,
                                 private val disposable: CompositeDisposable,
                                 private val navigator: VerificationIntroNavigator,
                                 private val logger: Logger,
                                 private val viewScheduler: Scheduler,
                                 private val ioScheduler: Scheduler,
                                 private val interactor: VerificationIntroInteractor,
                                 private val adyenErrorCodeMapper: AdyenErrorCodeMapper,
                                 private val data: VerificationIntroData,
                                 private val analytics: VerificationAnalytics) {

  companion object {
    private val TAG = VerificationIntroPresenter::class.java.name
  }

  fun present(savedInstanceState: Bundle?) {
    loadModel(savedInstanceState)
    handleCancelClicks()
    handleForgetCardClick()
    handleRetryClick(savedInstanceState)
    handleTryAgainClicks()
    handleSupportClicks()
  }

  private fun loadModel(savedInstanceState: Bundle?) {
    disposable.add(
        interactor.loadVerificationIntroModel()
            .subscribeOn(ioScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.finishCardConfiguration(it.paymentInfoModel.paymentMethodInfo!!,
                  it.paymentInfoModel.isStored, savedInstanceState)
              view.updateUi(it)
              view.hideLoading()
              view.unlockRotation()
              handleSubmitClicks(it.verificationInfoModel)
            }
            .doOnSubscribe {
              view.lockRotation()
              view.showLoading()
            }
            .subscribe({}, {
              logger.log(TAG, it)
              view.showGenericError()
            })
    )
  }

  private fun handleCancelClicks() {
    disposable.add(
        view.getCancelClicks()
            .doOnNext {
              analytics.sendInsertCardEvent("cancel")
              view.cancel()
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleRetryClick(savedInstanceState: Bundle?) {
    disposable.add(view.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showLoading() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext { loadModel(savedInstanceState) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleTryAgainClicks() {
    disposable.add(view.getTryAgainClicks()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .doOnNext { loadModel(null) }
        .observeOn(viewScheduler)
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleSupportClicks() {
    disposable.add(view.getSupportClicks()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapCompletable { interactor.showSupport() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleSubmitClicks(verificationInfoModel: VerificationInfoModel) {
    disposable.add(
        view.getSubmitClicks()
            .doOnNext {
              analytics.sendInsertCardEvent("get_code")
            }
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
            .observeOn(ioScheduler)
            .flatMapSingle { adyenCard ->
              interactor.makePayment(adyenCard.cardPaymentMethod, adyenCard.shouldStoreCard,
                  data.returnUrl)
            }
            .observeOn(viewScheduler)
            .flatMapCompletable {
              analytics.sendRequestConclusionEvent(it.success, it.refusalCode?.toString(),
                  it.refusalReason)
              handlePaymentResult(it, verificationInfoModel)
            }
            .subscribe({}, {
              logger.log(TAG, it)
              view.showGenericError()
            })
    )
  }

  private fun handlePaymentResult(paymentModel: VerificationPaymentModel,
                                  verificationInfoModel: VerificationInfoModel): Completable {
    return when {
      paymentModel.success -> {
        Completable.complete()
            .observeOn(viewScheduler)
            .andThen(handleSuccessTransaction(verificationInfoModel))
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
        handleErrors(paymentModel.error)
      }
      else -> Completable.fromAction {
        view.showGenericError()
      }
    }
  }

  private fun handleSuccessTransaction(verificationInfoModel: VerificationInfoModel): Completable {
    val ts = System.currentTimeMillis()
    return Completable.fromAction {
      navigator.navigateToCodeView(verificationInfoModel.currency, verificationInfoModel.symbol,
          verificationInfoModel.value, verificationInfoModel.digits, verificationInfoModel.format,
          verificationInfoModel.period, ts)
    }
  }

  private fun handleErrors(error: Error) {
    when {
      error.isNetworkError -> view.showNetworkError()
      else -> view.showGenericError()
    }
  }

  private fun handleForgetCardClick() {
    disposable.add(view.forgetCardClick()
        .observeOn(viewScheduler)
        .doOnNext {
          view.showLoading()
          analytics.sendInsertCardEvent("change_card")
        }
        .observeOn(ioScheduler)
        .flatMapSingle { interactor.disablePayments() }
        .observeOn(viewScheduler)
        .doOnNext { success -> if (!success) view.showGenericError() }
        .filter { it }
        .observeOn(ioScheduler)
        .flatMapSingle {
          interactor.loadVerificationIntroModel()
              .observeOn(viewScheduler)
              .doOnSuccess {
                view.updateUi(it)
                view.finishCardConfiguration(it.paymentInfoModel.paymentMethodInfo!!, false, null)
              }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }))
  }

  fun stop() = disposable.clear()
}
