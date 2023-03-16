package com.asfoundation.wallet.verification.ui.credit_card.intro

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel.ErrorType
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.appcoins.wallet.core.utils.common.extensions.isNoNetworkException
import com.asfoundation.wallet.verification.ui.credit_card.VerificationAnalytics
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.Serializable
import java.util.concurrent.TimeUnit

class VerificationIntroPresenter(
  private val view: VerificationIntroView,
  private val disposable: CompositeDisposable,
  private val navigator: VerificationIntroNavigator,
  private val logger: Logger,
  private val viewScheduler: Scheduler,
  private val ioScheduler: Scheduler,
  private val interactor: VerificationIntroInteractor,
  private val adyenErrorCodeMapper: AdyenErrorCodeMapper,
  private val data: VerificationIntroData,
  private val analytics: VerificationAnalytics
) {

  private var currentError: ErrorState? = null

  companion object {
    private const val CURRENT_ERROR_KEY = "current_error"
    private val TAG = VerificationIntroPresenter::class.java.name
  }

  fun present(savedInstanceState: Bundle?) {
    handleViewState(savedInstanceState)
    handleCancelClicks()
    handleForgetCardClick()
    handleRetryClick()
    handleTryAgainClicks()
    handleSupportClicks()
  }

  private fun handleViewState(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      currentError = savedInstanceState.get(CURRENT_ERROR_KEY) as ErrorState?
    }
    if (currentError == null) loadModel()
    else {
      when {
        currentError?.errorType != null -> view.showError(currentError!!.errorType)
        currentError?.errorString != null -> view.showSpecificError(currentError!!.errorString!!)
        else -> loadModel()
      }
    }
  }

  private fun loadModel(forgetPrevious: Boolean = false) {
    disposable.add(
      interactor.loadVerificationIntroModel()
        .subscribeOn(ioScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.finishCardConfiguration(it.paymentInfoModel, forgetPrevious)
          view.updateUi(it)
          hideLoading()
          handleSubmitClicks(it.verificationInfoModel)
        }
        .doOnSubscribe { showLoading() }
        .subscribe({}, {
          logger.log(TAG, it)
          handleErrors(it.isNoNetworkException())
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

  private fun handleRetryClick() {
    disposable.add(view.retryClick()
      .observeOn(viewScheduler)
      .doOnNext { showLoading() }
      .delay(1, TimeUnit.SECONDS)
      .doOnNext { loadModel(true) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleTryAgainClicks() {
    disposable.add(
      view.getTryAgainClicks()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .doOnNext { loadModel(true) }
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
          showLoading()
          view.hideKeyboard()
        }
        .observeOn(ioScheduler)
        .flatMapSingle { adyenCard ->
          interactor.makePayment(
            adyenCard.cardPaymentMethod, adyenCard.shouldStoreCard,
            data.returnUrl
          )
        }
        .observeOn(viewScheduler)
        .flatMapCompletable {
          analytics.sendRequestConclusionEvent(
            it.success, it.refusalCode?.toString(),
            it.refusalReason
          )
          handlePaymentResult(it, verificationInfoModel)
        }
        .subscribe({}, {
          logger.log(TAG, it)
          hideLoading()
          handleErrors()
        })
    )
  }

  private fun handlePaymentResult(
    paymentModel: VerificationPaymentModel,
    verificationInfoModel: VerificationInfoModel
  ): Completable {
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
            else -> {
              logger.log(
                TAG,
                Exception("PaymentResult code=$code reason=${paymentModel.refusalReason}")
              )
              handleErrors(errorString = adyenErrorCodeMapper.map(code))
            }
          }
        }
      }
      paymentModel.error.hasError -> Completable.fromAction {
        if (!paymentModel.error.isNetworkError) logger.log(
          TAG,
          Exception("PaymentResult type=${paymentModel.error.errorInfo?.errorType} code=${paymentModel.error.errorInfo?.httpCode}")
        )
        handleErrors(paymentModel.error.isNetworkError, paymentModel.errorType)
      }
      else -> Completable.fromAction {
        logger.log(TAG, Exception("PaymentResult code=${paymentModel.refusalCode}"))
        handleErrors()
      }
    }
  }

  private fun handleSuccessTransaction(verificationInfoModel: VerificationInfoModel): Completable {
    val ts = System.currentTimeMillis()
    return Completable.fromAction {
      navigator.navigateToCodeView(
        verificationInfoModel.currency, verificationInfoModel.symbol,
        verificationInfoModel.value, verificationInfoModel.digits, verificationInfoModel.format,
        verificationInfoModel.period, ts
      )
      hideLoading()
    }
  }

  private fun handleErrors(
    noNetworkError: Boolean = false, errorType: ErrorType? = null,
    errorString: Int? = null
  ) {
    hideLoading()
    currentError = ErrorState(noNetworkError, errorType, errorString)
    when {
      noNetworkError -> view.showNetworkError()
      errorType != null -> view.showError(errorType)
      errorString != null -> view.showSpecificError(errorString)
      else -> view.showGenericError()
    }
  }

  private fun handleForgetCardClick() {
    disposable.add(view.forgetCardClick()
      .observeOn(viewScheduler)
      .doOnNext {
        showLoading()
        analytics.sendInsertCardEvent("change_card")
      }
      .observeOn(ioScheduler)
      .flatMapSingle { interactor.disablePayments() }
      .observeOn(viewScheduler)
      .doOnNext { success ->
        if (!success) {
          hideLoading()
          logger.log(TAG, Exception("ForgetCardClick"))
          handleErrors()
        }
      }
      .filter { it }
      .observeOn(ioScheduler)
      .flatMapSingle {
        interactor.loadVerificationIntroModel()
          .observeOn(viewScheduler)
          .doOnSuccess {
            hideLoading()
            view.updateUi(it)
            view.finishCardConfiguration(it.paymentInfoModel, forget = true)
          }
      }
      .subscribe({}, {
        logger.log(TAG, it)
        hideLoading()
        handleErrors(it.isNoNetworkException())
      })
    )
  }

  private fun showLoading() {
    view.lockRotation()
    view.showLoading()
  }

  private fun hideLoading() {
    view.unlockRotation()
    view.hideLoading()
  }

  fun onSavedInstance(outState: Bundle) {
    outState.putSerializable(CURRENT_ERROR_KEY, currentError)
  }

  fun stop() = disposable.clear()
}

data class ErrorState(
  val noNetworkError: Boolean, val errorType: ErrorType?,
  val errorString: Int?
) : Serializable