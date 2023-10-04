package com.asfoundation.wallet.ui.iab.localpayments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import com.appcoins.wallet.appcoins.rewards.ErrorInfo.ErrorType
import com.appcoins.wallet.appcoins.rewards.ErrorMapper
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.network.microservices.model.Transaction.Status
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class LocalPaymentPresenter(
  private val view: LocalPaymentView,
  private val data: LocalPaymentData,
  private val localPaymentInteractor: LocalPaymentInteractor,
  private val navigator: LocalPaymentNavigator,
  private val analytics: LocalPaymentAnalytics,
  private val viewScheduler: Scheduler,
  private val networkScheduler: Scheduler,
  private val disposables: CompositeDisposable,
  private val context: Context?,
  private val logger: Logger,
  private val errorMapper: ErrorMapper
) {

  private var waitingResult: Boolean = false

  fun present(savedInstance: Bundle?) {
    view.setupUi(data.bonus)
    savedInstance?.let {
      waitingResult = savedInstance.getBoolean(WAITING_RESULT)
    }
    onViewCreatedRequestLink()
    handlePaymentRedirect()
    handleOkErrorButtonClick()
    handleOkBuyButtonClick()
    handleSupportClicks()
  }

  fun handleStop() {
    waitingResult = false
    disposables.clear()
  }

  fun preparePendingUserPayment() {
    disposables.add(
      Single.zip(
        getPaymentMethodIcon(),
        getApplicationIcon()
      ) { paymentMethodIcon: Bitmap, applicationIcon: Bitmap ->
        Pair(paymentMethodIcon, applicationIcon)
      }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe(
          { view.showPendingUserPayment(data.label, it.first, it.second) },
          { showError(it) }
        )
    )
  }

  private fun getPaymentMethodIcon() = Single.fromCallable {
    GlideApp.with(context!!)
      .asBitmap()
      .load(data.paymentMethodIconUrl)
      .override(getWidth(), getHeight())
      .centerCrop()
      .submit()
      .get()
  }

  private fun getApplicationIcon() = Single.fromCallable {
    val applicationIcon =
      (context!!.packageManager.getApplicationIcon(data.packageName) as BitmapDrawable).bitmap

    Bitmap.createScaledBitmap(applicationIcon, appIconWidth, appIconHeight, true)
  }

  private fun onViewCreatedRequestLink() {
    disposables.add(
      localPaymentInteractor.getPaymentLink(
        data.paymentId,data.packageName, data.fiatAmount,
        data.currency, data.skuId, data.type, data.origin,
        data.developerAddress, data.payload, data.callbackUrl, data.orderReference,
        data.referrerUrl
      )
        .filter { !waitingResult }
        .observeOn(viewScheduler)
        .doOnSuccess {
          analytics.sendNavigationToUrlEvents(
            data.packageName, data.skuId,
            data.appcAmount.toString(), data.type, data.paymentId
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
      .doOnNext { view.showProcessingLoading() }
      .doOnNext { view.lockRotation() }
      .flatMap {
        localPaymentInteractor.getTransaction(it, data.async)
          .subscribeOn(networkScheduler)
      }
      .observeOn(viewScheduler)
      .flatMapCompletable { handleTransactionStatus(it) }
      .subscribe({}, { showError(it) })
    )
  }

  private fun handleOkErrorButtonClick() {
    disposables.add(view.getErrorDismissClick()
      .doOnNext { view.dismissError() }
      .subscribe({}, { view.dismissError() })
    )
  }

  private fun handleOkBuyButtonClick() {
    disposables.add(view.getGotItClick()
      .doOnNext { view.close() }
      .subscribe({}, { view.close() })
    )
  }

  private fun handleFraudFlow() {
    disposables.add(localPaymentInteractor.isWalletBlocked()
      .subscribeOn(networkScheduler)
      .observeOn(networkScheduler)
      .flatMap { blocked ->
        if (blocked) {
          localPaymentInteractor.isWalletVerified()
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it) {
                logger.log(TAG, Exception("FraudFlow blocked"))
                view.showError(R.string.purchase_error_wallet_block_code_403)
              } else {
                view.showVerification()
              }
            }
        } else {
          Single.just(true)
            .observeOn(viewScheduler)
            .doOnSuccess {
              logger.log(TAG, Exception("FraudFlow not blocked"))
              view.showError(R.string.purchase_error_wallet_block_code_403)
            }
        }
      }
      .observeOn(viewScheduler)
      .subscribe({}, {
        logger.log(TAG, it)
        view.showError(R.string.purchase_error_wallet_block_code_403)
      })
    )
  }

  private fun handleTransactionStatus(transaction: Transaction): Completable {
    view.hideLoading()
    return when {
      isErrorStatus(transaction) -> Completable.fromAction {
        logger.log(TAG, "Transaction came with error status: ${transaction.status}")
        view.showError()
      }
        .subscribeOn(viewScheduler)
      data.async ->
        //Although this should no longer happen at the moment in Iab, since it doesn't consume much process time
        //I decided to leave this here in case the API wants to change the logic and return them to Iab in the future.
        handleAsyncTransactionStatus(transaction)
          .andThen(Completable.fromAction {
            localPaymentInteractor.savePreSelectedPaymentMethod(data.paymentId)
            localPaymentInteractor.saveAsyncLocalPayment(data.paymentId)
            preparePendingUserPayment()
          })
      transaction.status == Status.COMPLETED -> handleSyncCompletedStatus(transaction)
      else -> Completable.complete()
    }
  }

  private fun isErrorStatus(transaction: Transaction) =
    transaction.status == Status.FAILED ||
        transaction.status == Status.CANCELED ||
        transaction.status == Status.INVALID_TRANSACTION

  private fun handleSyncCompletedStatus(transaction: Transaction): Completable {
    return localPaymentInteractor.getCompletePurchaseBundle(
      data.type, data.packageName, data.skuId,
      transaction.metadata?.purchaseUid, transaction.orderReference, transaction.hash,
      networkScheduler
    )
      .doOnSuccess {
        analytics.sendPaymentConclusionEvents(
          packageName = data.packageName,
          skuId = data.skuId,
          amount = data.appcAmount,
          type = data.type,
          paymentId = data.paymentId,
          txId = transaction.uid,
          amountUsd = TransactionBuilder.convertAppcToUsd(data.appcAmount)
        )
        handleRevenueEvent()
      }
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .flatMapCompletable {
        Completable.fromAction { view.showCompletedPayment() }
          .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS, viewScheduler))
          .andThen(Completable.fromAction { view.popView(it.bundle, data.paymentId) })
      }
  }

  private fun handleRevenueEvent() {
    disposables.add(localPaymentInteractor.convertToFiat(
      data.appcAmount.toDouble(),
      BillingAnalytics.EVENT_REVENUE_CURRENCY
    )
      .subscribeOn(networkScheduler)
      .doOnSuccess { fiatValue -> analytics.sendRevenueEvent(fiatValue.amount.toString()) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleAsyncTransactionStatus(transaction: Transaction): Completable {
    return when (transaction.status) {
      Status.PENDING_USER_PAYMENT -> {
        Completable.fromAction {
          analytics.sendPendingPaymentEvents(
            data.packageName, data.skuId,
            data.appcAmount.toString(), data.type, data.paymentId
          )
        }
      }
      Status.COMPLETED -> {
        Completable.fromAction {
          analytics.sendPaymentConclusionEvents(
            packageName = data.packageName,
            skuId = data.skuId,
            amount = data.appcAmount,
            type = data.type,
            paymentId = data.paymentId,
            txId = transaction.uid,
            amountUsd = TransactionBuilder.convertAppcToUsd(data.appcAmount)
          )
          handleRevenueEvent()
        }
      }
      else -> Completable.complete()
    }
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .flatMapCompletable { localPaymentInteractor.showSupport(data.gamificationLevel) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    val error = errorMapper.map(throwable)
    when (error.errorType) {
      ErrorType.SUB_ALREADY_OWNED -> view.showError(R.string.subscriptions_error_already_subscribed)
      ErrorType.BLOCKED -> handleFraudFlow()
      else -> view.showError(R.string.unknown_error)
    }
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
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

  private val appIconWidth: Int
    get() = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX, 160f,
      context?.resources?.displayMetrics
    )
      .toInt()

  private val appIconHeight: Int
    get() = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX, 160f,
      context?.resources?.displayMetrics
    )
      .toInt()

  companion object {
    private val TAG = LocalPaymentPresenter::class.java.simpleName
    private const val WAITING_RESULT = "WAITING_RESULT"
  }
}

