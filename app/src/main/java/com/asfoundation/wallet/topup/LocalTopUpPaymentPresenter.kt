package com.asfoundation.wallet.topup

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.iab.LocalPaymentInteractor
import com.asfoundation.wallet.ui.iab.Navigator
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class LocalTopUpPaymentPresenter(
    private val view: LocalTopUpPaymentView,
    private val activityView: TopUpActivityView,
    private val context: Context?,
    private val localPaymentInteractor: LocalPaymentInteractor,
    private val analytics: TopUpAnalytics,
    private val navigator: Navigator,
    private val formatter: CurrencyFormatUtils,
    private val billingMessagesMapper: BillingMessagesMapper,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val disposables: CompositeDisposable,
    private val data: TopUpPaymentData,
    private val paymentId: String,
    private val paymentIcon: String,
    private val packageName: String) {

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
    val fiatAmount = formatter.formatCurrency(data.fiatValue, WalletCurrency.FIAT)
    val appcAmount = formatter.formatCurrency(data.appcValue, WalletCurrency.CREDITS)
    view.showValues(fiatAmount, data.fiatCurrencyCode, appcAmount)
  }

  private fun preparePendingUserPayment() {
    disposables.add(getPaymentMethodIcon()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          status = ViewState.PENDING_USER_PAYMENT
          view.showPendingUserPayment(it)
        }
        .subscribe({}, { showError(it) }))
  }

  private fun getPaymentMethodIcon(): Single<Bitmap> {
    return Single.fromCallable {
      GlideApp.with(context!!)
          .asBitmap()
          .load(paymentIcon)
          .override(getWidth(), getHeight())
          .centerCrop()
          .submit()
          .get()
    }
  }

  private fun onViewCreatedRequestLink() {
    disposables.add(localPaymentInteractor.getTopUpPaymentLink(packageName, data.fiatValue,
        data.fiatCurrencyCode, paymentId)
        .filter { !waitingResult && it.isNotEmpty() }
        .observeOn(viewScheduler)
        .doOnSuccess {
          analytics.sendConfirmationEvent(data.appcValue.toDouble(), "top_up", paymentId)
          navigator.navigateToUriForResult(it)
          waitingResult = true
        }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe({ }, { showError(it) }))
  }

  private fun handlePaymentRedirect() {
    disposables.add(navigator.uriResults()
        .doOnNext {
          activityView.lockOrientation()
          status = ViewState.LOADING
          view.showProcessingLoading()
        }
        .flatMap {
          localPaymentInteractor.getTransaction(it)
              .subscribeOn(networkScheduler)
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handleTransactionStatus(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handleTryAgainClick() {
    disposables.add(view.getTryAgainClick()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .doOnNext { view.navigateToPaymentSelection() }
        .subscribe())
  }

  private fun handleGotItClick() {
    disposables.add(view.getGotItClick()
        .doOnNext { activityView.close() }
        .subscribe()
    )
  }

  private fun handleTransactionStatus(transaction: Transaction): Completable {
    return when {
      isErrorStatus(transaction) -> Completable.fromAction { showGenericError() }
      transaction.status == Transaction.Status.COMPLETED -> handleSyncCompletedStatus()
      localPaymentInteractor.isAsync(transaction.type) ->
        Completable.fromAction {
          analytics.sendSuccessEvent(data.appcValue.toDouble(), paymentId, "pending")
        }.andThen(Completable.fromAction { preparePendingUserPayment() })
      else -> Completable.complete()
    }
  }

  private fun isErrorStatus(transaction: Transaction) =
      transaction.status == Transaction.Status.FAILED ||
          transaction.status == Transaction.Status.CANCELED ||
          transaction.status == Transaction.Status.INVALID_TRANSACTION

  private fun handleSyncCompletedStatus(): Completable {
    return Completable.fromAction {
      analytics.sendSuccessEvent(data.appcValue.toDouble(), paymentId, "success")
      val bundle = createBundle(data.fiatValue, data.fiatCurrencyCode, data.fiatCurrencySymbol)
      waitingResult = false
      navigator.popView(bundle)
    }
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapCompletable { localPaymentInteractor.showSupport(data.gamificationLevel) }
        .subscribe()
    )
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      status = ViewState.NO_NETWORK
      view.showNetworkError()
    } else showGenericError()
  }

  private fun showGenericError() {
    status = ViewState.GENERIC_ERROR
    view.showError()
  }

  private fun createBundle(priceAmount: String, priceCurrency: String,
                           fiatCurrencySymbol: String): Bundle {
    return billingMessagesMapper.topUpBundle(priceAmount, priceCurrency,
        data.bonusValue.toPlainString(), fiatCurrencySymbol)
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
        .doOnNext { view.navigateToPaymentSelection() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun getWidth(): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 184f,
        context?.resources?.displayMetrics)
        .toInt()
  }

  private fun getHeight(): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 80f,
        context?.resources?.displayMetrics)
        .toInt()
  }

  fun stop() {
    waitingResult = false
    disposables.clear()
  }

  companion object {
    private const val WAITING_RESULT = "WAITING_RESULT"
    private const val STATUS_KEY = "status"
  }
}
