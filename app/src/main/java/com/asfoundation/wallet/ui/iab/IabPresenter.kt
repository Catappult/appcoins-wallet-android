package com.asfoundation.wallet.ui.iab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class IabPresenter(private val view: IabView,
                   private val networkScheduler: Scheduler,
                   private val viewScheduler: Scheduler,
                   private val disposable: CompositeDisposable,
                   private val billingAnalytics: BillingAnalytics,
                   private val iabInteract: IabInteract,
                   private val logger: Logger,
                   private val transaction: TransactionBuilder?) {

  private var firstImpression = true

  companion object {
    private val TAG = IabActivity::class.java.name
    private const val FIRST_IMPRESSION = "first_impression"
  }

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      firstImpression = it.getBoolean(FIRST_IMPRESSION, firstImpression)
    }
    if (savedInstanceState == null) {
      handlePurchaseStartAnalytics(transaction)
      view.showPaymentMethodsView()
    }
  }

  fun onResume() {
    handleAutoUpdate()
    handleUserRegistration()
    handleSupportClicks()
    handleErrorDismisses()
  }

  private fun handleErrorDismisses() {
    disposable.add(view.errorDismisses()
        .doOnNext { view.close(Bundle()) }
        .subscribe({ }, { view.close(Bundle()) }))
  }

  private fun handleSupportClicks() {
    disposable.add(view.getSupportClicks()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext { iabInteract.showSupport() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleWalletBlockedCheck(@StringRes error: Int) {
    disposable.add(iabInteract.isWalletBlocked()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it) view.showError(error)
          else view.showPaymentMethodsView()
        }
        .subscribe({}, { handleError(it) })
    )
  }

  private fun handleError(throwable: Throwable) {
    logger.log(TAG, throwable)
    view.finishWithError()
  }

  fun handlePerkNotifications(bundle: Bundle) {
    disposable.add(iabInteract.getWalletAddress()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.launchPerkBonusService(it)
          view.finishActivity(bundle)
        }
        .doOnError { view.finishActivity(bundle) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun handleBackupNotifications(bundle: Bundle) {
    disposable.add(iabInteract.incrementAndValidateNotificationNeeded()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { notificationNeeded ->
          if (notificationNeeded.isNeeded) {
            view.showBackupNotification(notificationNeeded.walletAddress)
          }
          view.finishActivity(bundle)
        }
        .doOnError { view.finish(bundle) }
        .subscribe({ }, { it.printStackTrace() })
    )
  }

  fun handlePurchaseStartAnalytics(transaction: TransactionBuilder?) {
    disposable.add(Completable.fromAction {
      if (firstImpression) {
        if (iabInteract.hasPreSelectedPaymentMethod()) {
          billingAnalytics.sendPurchaseStartEvent(transaction?.domain, transaction?.skuId,
              transaction?.amount()
                  .toString(), iabInteract.getPreSelectedPaymentMethod(),
              transaction?.type, BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD)
        } else {
          billingAnalytics.sendPurchaseStartWithoutDetailsEvent(transaction?.domain,
              transaction?.skuId, transaction?.amount()
              .toString(), transaction?.type,
              BillingAnalytics.RAKAM_PAYMENT_METHOD)
        }
        firstImpression = false
      }
    }
        .subscribeOn(networkScheduler)
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleAutoUpdate() {
    disposable.add(iabInteract.getAutoUpdateModel()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .filter {
          iabInteract.isHardUpdateRequired(it.blackList, it.updateVersionCode, it.updateMinSdk)
        }
        .doOnSuccess { view.showUpdateRequiredView() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleUserRegistration() {
    disposable.add(iabInteract.registerUser()
        .subscribeOn(networkScheduler)
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposable.clear()

  fun onSaveInstance(outState: Bundle) {
    outState.putBoolean(FIRST_IMPRESSION, firstImpression)
  }

  fun savePreselectedPaymentMethod(bundle: Bundle) {
    bundle.getString(PRE_SELECTED_PAYMENT_METHOD_KEY)
        ?.let {
          iabInteract.savePreSelectedPaymentMethod(it)
        }
  }

  private fun sendPayPalConfirmationEvent(action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(transaction?.domain, transaction?.skuId,
        transaction?.amount()
            .toString(), "paypal",
        transaction?.type, action)
  }

  private fun sendCarrierBillingConfirmationEvent(action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(transaction?.domain, transaction?.skuId,
        transaction?.amount()
            .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
        transaction?.type, action)
  }

  private fun sendPaypalUrlEvent(data: Intent) {
    val amountString = transaction?.amount()
        .toString()
    billingAnalytics.sendPaypalUrlEvent(transaction?.domain, transaction?.skuId,
        amountString, "PAYPAL", getQueryParameter(data, "type"),
        getQueryParameter(data, "resultCode"), data.dataString)
  }

  private fun getQueryParameter(data: Intent, parameter: String): String? {
    return Uri.parse(data.dataString)
        .getQueryParameter(parameter)
  }

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      IabActivity.WEB_VIEW_REQUEST_CODE -> handleWebViewResult(resultCode, data)
      IabActivity.WALLET_VALIDATION_REQUEST_CODE -> handleWalletValidationResult(data)
      IabActivity.AUTHENTICATION_REQUEST_CODE -> handleAuthenticationResult(resultCode)
    }
  }

  private fun handleWebViewResult(resultCode: Int, data: Intent?) {
    if (resultCode == WebViewActivity.FAIL) {
      if (data?.dataString?.contains("codapayments") != true) {
        if (data?.dataString?.contains(
                BillingWebViewFragment.CARRIER_BILLING_ONE_BIP_SCHEMA) == true) {
          sendCarrierBillingConfirmationEvent("cancel")
        } else {
          sendPayPalConfirmationEvent("cancel")
        }
      }
      if (data?.dataString?.contains(BillingWebViewFragment.OPEN_SUPPORT) == true) {
        iabInteract.showSupport()
      }
      view.showPaymentMethodsView()
    } else if (resultCode == WebViewActivity.SUCCESS) {
      if (data?.scheme?.contains("adyencheckout") == true) {
        sendPaypalUrlEvent(data)
        if (getQueryParameter(data, "resultCode") == "cancelled")
          sendPayPalConfirmationEvent("cancel")
        else
          sendPayPalConfirmationEvent("buy")
      }
      view.successWebViewResult(data!!.data)
    }
  }

  private fun handleWalletValidationResult(data: Intent?) {
    var errorMessage = data?.getIntExtra(IabActivity.ERROR_MESSAGE, 0)
    if (errorMessage == null || errorMessage == 0) {
      errorMessage = R.string.unknown_error
    }
    handleWalletBlockedCheck(errorMessage)
  }

  private fun handleAuthenticationResult(resultCode: Int) {
    if (resultCode == AuthenticationPromptActivity.RESULT_OK) {
      view.authenticationResult(true)
    } else if (resultCode == AuthenticationPromptActivity.RESULT_CANCELED) {
      view.authenticationResult(false)
    }
  }
}