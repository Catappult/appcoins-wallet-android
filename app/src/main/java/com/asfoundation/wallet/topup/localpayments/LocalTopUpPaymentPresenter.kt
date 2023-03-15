package com.asfoundation.wallet.topup.localpayments

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.ui.iab.Navigator
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentInteractor
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.common.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class LocalTopUpPaymentPresenter(
  private val view: LocalTopUpPaymentView,
  private val context: Context?,
  private val localPaymentInteractor: LocalPaymentInteractor,
  private val analytics: TopUpAnalytics,
  private val navigator: Navigator,
  private val formatter: CurrencyFormatUtils,
  private val viewScheduler: Scheduler,
  private val networkScheduler: Scheduler,
  private val disposables: CompositeDisposable,
  private val data: LocalTopUpPaymentData,
  private val logger: Logger
) {

  private var waitingResult: Boolean = false
  private var status: ViewState = ViewState.NONE

  fun present(savedInstance: Bundle?) {
    setupUi()
    if (savedInstance != null) {
      waitingResult = savedInstance.getBoolean(WAITING_RESULT)
      status = savedInstance.get(STATUS_KEY) as ViewState
    }
    handleViewInitialization(status)
    handlePaymentRedirect()
    handleTryAgainClick()
    handleGotItClick()
    handleSupportClicks()
    handleRetryClick()
  }

  private fun handleViewInitialization(status: ViewState) {
    when (status) {
      ViewState.PENDING_USER_PAYMENT -> preparePendingUserPayment()
      ViewState.GENERIC_ERROR -> view.showError()
      ViewState.NO_NETWORK -> view.showNetworkError()
      ViewState.LOADING -> view.showProcessingLoading()
      ViewState.NONE -> onViewCreatedRequestLink()
    }
  }

  private fun setupUi() {
    val fiatAmount = formatter.formatCurrency(data.topUpData.fiatValue, WalletCurrency.FIAT)
    val appcAmount = formatter.formatCurrency(data.topUpData.appcValue, WalletCurrency.CREDITS)
    view.showValues(
      fiatAmount, data.topUpData.fiatCurrencyCode, appcAmount,
      data.topUpData.selectedCurrencyType
    )
  }

  private fun preparePendingUserPayment() {
    disposables.add(getPaymentMethodIcon()
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSuccess {
        status = ViewState.PENDING_USER_PAYMENT
        view.showPendingUserPayment(it, data.paymentLabel)
      }
      .subscribe({}, { showError(it) })
    )
  }

  private fun getPaymentMethodIcon(): Single<Bitmap> {
    return Single.fromCallable {
      GlideApp.with(context!!)
        .asBitmap()
        .load(data.paymentIcon)
        .override(getWidth(), getHeight())
        .centerCrop()
        .submit()
        .get()
    }
  }

  private fun onViewCreatedRequestLink() {
    disposables.add(
      localPaymentInteractor.getTopUpPaymentLink(
        data.packageName, data.topUpData.fiatValue,
        data.topUpData.fiatCurrencyCode, data.paymentId,
        context?.getString(R.string.topup_title) ?: "Top up"
      )
        .filter { !waitingResult && it.isNotEmpty() }
        .observeOn(viewScheduler)
        .doOnSuccess {
          analytics.sendConfirmationEvent(
            data.topUpData.appcValue.toDouble(), "top_up",
            data.paymentId
          )
          navigator.navigateToUriForResult(it)
          waitingResult = true
        }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe({ }, { showError(it) })
    )
  }

  private fun handlePaymentRedirect() {
    disposables.add(navigator.uriResults()
      .doOnNext {
        status = ViewState.LOADING
        view.showProcessingLoading()
      }
      .flatMap {
        localPaymentInteractor.getTransaction(it, data.async)
          .subscribeOn(networkScheduler)
      }
      .observeOn(viewScheduler)
      .flatMapCompletable { handleTransactionStatus(it) }
      .subscribe({}, { showError(it) })
    )
  }

  private fun handleTryAgainClick() {
    disposables.add(view.getTryAgainClick()
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .doOnNext { navigator.navigateBack() }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleGotItClick() {
    disposables.add(view.getGotItClick()
      .doOnNext { view.close() }
      .subscribe({}, { view.close() })
    )
  }

  private fun handleTransactionStatus(transaction: Transaction): Completable {
    return when {
      isErrorStatus(transaction) -> Completable.fromAction {
        logger.log(
          TAG, "Transaction came with error status: ${transaction.status}"
        )
        showGenericError()
      }
      transaction.status == Transaction.Status.COMPLETED -> handleSyncCompletedStatus()
      data.async -> Completable.fromAction {
        analytics.sendSuccessEvent(data.topUpData.appcValue.toDouble(), data.paymentId, "pending")
      }
        .andThen(Completable.fromAction { preparePendingUserPayment() })
      else -> Completable.complete()
    }
  }

  private fun isErrorStatus(transaction: Transaction) =
    transaction.status == Transaction.Status.FAILED ||
        transaction.status == Transaction.Status.CANCELED ||
        transaction.status == Transaction.Status.INVALID_TRANSACTION

  private fun handleSyncCompletedStatus(): Completable {
    return Completable.fromAction {
      analytics.sendSuccessEvent(data.topUpData.appcValue.toDouble(), data.paymentId, "success")
      val bundle = createBundle(
        data.topUpData.fiatValue, data.topUpData.fiatCurrencyCode,
        data.topUpData.fiatCurrencySymbol
      )
      waitingResult = false
      navigator.popView(bundle)
    }
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .flatMapCompletable { localPaymentInteractor.showSupport(data.topUpData.gamificationLevel) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    if (throwable.isNoNetworkException()) {
      status = ViewState.NO_NETWORK
      view.showNetworkError()
    } else showGenericError()
  }

  private fun showGenericError() {
    status = ViewState.GENERIC_ERROR
    view.showError()
  }

  private fun createBundle(
    priceAmount: String, priceCurrency: String,
    fiatCurrencySymbol: String
  ): Bundle {
    return localPaymentInteractor.topUpBundle(
      priceAmount, priceCurrency,
      data.topUpData.bonusValue.toPlainString(), fiatCurrencySymbol
    )
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putSerializable(STATUS_KEY, status)
    outState.putBoolean(WAITING_RESULT, waitingResult)
  }

  private fun handleRetryClick() {
    disposables.add(view.retryClick()
      .observeOn(viewScheduler)
      .doOnNext { view.showRetryAnimation() }
      .delay(1, TimeUnit.SECONDS)
      .doOnNext { navigator.navigateBack() }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun getWidth(): Int {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX, 184f,
      context?.resources?.displayMetrics
    )
      .toInt()
  }

  private fun getHeight(): Int {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX, 80f,
      context?.resources?.displayMetrics
    )
      .toInt()
  }

  fun stop() {
    waitingResult = false
    disposables.clear()
  }

  companion object {
    private val TAG = LocalTopUpPaymentPresenter::class.java.simpleName
    private const val WAITING_RESULT = "WAITING_RESULT"
    private const val STATUS_KEY = "status"
  }
}
