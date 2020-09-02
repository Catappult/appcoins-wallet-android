package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.StringRes
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import retrofit2.HttpException
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


class LocalPaymentPresenter(private val view: LocalPaymentView,
                            private val originalAmount: String?,
                            private val currency: String?,
                            private val domain: String,
                            private val skuId: String?,
                            private val paymentId: String,
                            private val developerAddress: String,
                            private val localPaymentInteractor: LocalPaymentInteractor,
                            private val navigator: FragmentNavigator,
                            private val type: String,
                            private val amount: BigDecimal,
                            private val analytics: LocalPaymentAnalytics,
                            private val savedInstance: Bundle?,
                            private val viewScheduler: Scheduler,
                            private val networkScheduler: Scheduler,
                            private val disposables: CompositeDisposable,
                            private val callbackUrl: String?,
                            private val orderReference: String?,
                            private val payload: String?,
                            private val context: Context?,
                            private val paymentMethodIconUrl: String?,
                            private val gamificationLevel: Int,
                            private val logger: Logger) {

  private var waitingResult: Boolean = false

  fun present() {
    if (savedInstance != null) {
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
            getApplicationIcon(),
            BiFunction { paymentMethodIcon: Bitmap, applicationIcon: Bitmap ->
              Pair(paymentMethodIcon, applicationIcon)
            }
        )
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .subscribe({ view.showPendingUserPayment(it.first, it.second) }, { showError(it) }))
  }

  private fun getPaymentMethodIcon() = Single.fromCallable {
    GlideApp.with(context!!)
        .asBitmap()
        .load(paymentMethodIconUrl)
        .override(getWidth(), getHeight())
        .centerCrop()
        .submit()
        .get()
  }

  private fun getApplicationIcon() = Single.fromCallable {
    val applicationIcon =
        (context!!.packageManager.getApplicationIcon(domain) as BitmapDrawable).bitmap

    Bitmap.createScaledBitmap(applicationIcon, appIconWidth, appIconHeight, true)
  }

  private fun onViewCreatedRequestLink() {
    disposables.add(
        localPaymentInteractor.getPaymentLink(domain, skuId, originalAmount, currency, paymentId,
            developerAddress, callbackUrl, orderReference, payload)
            .filter { !waitingResult }
            .observeOn(viewScheduler)
            .doOnSuccess {
              analytics.sendPaymentMethodDetailsEvent(domain, skuId, amount.toString(), type,
                  paymentId)
              analytics.sendPaymentConfirmationEvent(domain, skuId, amount.toString(), type,
                  paymentId)
              navigator.navigateToUriForResult(it)
              waitingResult = true
            }
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .subscribe({ }, { showError(it) }))
  }

  private fun handlePaymentRedirect() {
    disposables.add(navigator.uriResults()
        .doOnNext { view.showProcessingLoading() }
        .doOnNext { view.lockRotation() }
        .flatMap {
          localPaymentInteractor.getTransaction(it)
              .subscribeOn(networkScheduler)
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handleTransactionStatus(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handleOkErrorButtonClick() {
    disposables.add(view.getErrorDismissClick()
        .doOnNext { view.dismissError() }
        .subscribe({}, { view.dismissError() }))
  }

  private fun handleOkBuyButtonClick() {
    disposables.add(view.getGotItClick()
        .doOnNext { view.close() }
        .subscribe({}, { view.close() })
    )
  }

  private fun handleFraudFlow() {
    disposables.add(
        localPaymentInteractor.isWalletBlocked()
            .subscribeOn(networkScheduler)
            .observeOn(networkScheduler)
            .flatMap { blocked ->
              if (blocked) {
                localPaymentInteractor.isWalletVerified()
                    .observeOn(viewScheduler)
                    .doOnSuccess {
                      if (it) view.showError(R.string.purchase_error_wallet_block_code_403)
                      else view.showWalletValidation(R.string.purchase_error_wallet_block_code_403)
                    }
              } else {
                Single.just(true)
                    .observeOn(viewScheduler)
                    .doOnSuccess { view.showError(R.string.purchase_error_wallet_block_code_403) }
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
      localPaymentInteractor.isAsync(transaction.type) ->
        handleAsyncTransactionStatus(transaction)
            .andThen(Completable.fromAction {
              localPaymentInteractor.savePreSelectedPaymentMethod(paymentId)
              localPaymentInteractor.saveAsyncLocalPayment(paymentId)
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
    return localPaymentInteractor.getCompletePurchaseBundle(type, domain, skuId,
        transaction.metadata?.purchaseUid, transaction.orderReference, transaction.hash,
        networkScheduler)
        .doOnSuccess {
          analytics.sendPaymentEvent(domain, skuId, amount.toString(), type, paymentId)
          analytics.sendPaymentConclusionEvent(domain, skuId, amount.toString(), type,
              paymentId)
          analytics.sendRevenueEvent(disposables, amount)
        }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .flatMapCompletable {
          localPaymentInteractor.getWalletAddress()
              .flatMapCompletable { Completable.fromAction { view.launchPerkBonusService(it) } }
              .andThen(Completable.fromAction { view.showCompletedPayment() })
              .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS))
              .andThen(Completable.fromAction { view.popView(it) })
        }
  }

  private fun handleAsyncTransactionStatus(transaction: Transaction): Completable {
    return when (transaction.status) {
      Status.PENDING_USER_PAYMENT -> {
        Completable.fromAction {
          analytics.sendPaymentEvent(domain, skuId, amount.toString(), type, paymentId)
          analytics.sendPaymentPendingEvent(domain, skuId, amount.toString(), type, paymentId)
        }
      }
      Status.COMPLETED -> {
        Completable.fromAction {
          analytics.sendPaymentEvent(domain, skuId, amount.toString(), type, paymentId)
          analytics.sendPaymentConclusionEvent(domain, skuId, amount.toString(), type,
              paymentId)
          analytics.sendRevenueEvent(disposables, amount)
        }
      }
      else -> Completable.complete()
    }
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapCompletable { localPaymentInteractor.showSupport(gamificationLevel) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    if (throwable is HttpException && throwable.code() == FORBIDDEN_CODE) handleFraudFlow()
    else view.showError(mapError(throwable))
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
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

  private val appIconWidth: Int
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 160f,
        context?.resources?.displayMetrics)
        .toInt()

  private val appIconHeight: Int
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 160f,
        context?.resources?.displayMetrics)
        .toInt()

  companion object {
    private val TAG = LocalPaymentPresenter::class.java.simpleName
    private const val WAITING_RESULT = "WAITING_RESULT"
    private const val FORBIDDEN_CODE = 403
  }

  @StringRes
  private fun mapError(throwable: Throwable): Int {
    return when (throwable) {
      is HttpException -> mapHttpError(throwable)
      else -> R.string.unknown_error
    }
  }

  @StringRes
  private fun mapHttpError(exceptiont: HttpException): Int {
    return when (exceptiont.code()) {
      FORBIDDEN_CODE -> R.string.purchase_error_wallet_block_code_403
      else -> R.string.unknown_error
    }
  }
}

