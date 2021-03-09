package com.asfoundation.wallet.ui.iab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class IabPresenter(private val view: IabView,
                   private val networkScheduler: Scheduler,
                   private val viewScheduler: Scheduler,
                   private val disposable: CompositeDisposable,
                   private val billingAnalytics: BillingAnalytics,
                   private val iabInteract: IabInteract,
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
  }

  fun handlePerkNotifications(bundle: Bundle) {
    disposable.add(iabInteract.getWalletAddress()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.launchPerkBonusAndGamificationService(it)
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

  private fun handlePurchaseStartAnalytics(transaction: TransactionBuilder?) {
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
      IabActivity.AUTHENTICATION_REQUEST_CODE -> handleAuthenticationResult(resultCode)
    }
  }

  private fun handleWebViewResult(resultCode: Int, data: Intent?) {
    if (resultCode == WebViewActivity.FAIL) {
      if (data?.dataString?.contains("codapayments") != true) {
        if (data?.dataString?.contains(
                BillingWebViewFragment.CARRIER_BILLING_ONE_BIP_SCHEMA) == true) {
          sendCarrierBillingConfirmationEvent(WalletsAnalytics.ACTION_CANCEL)
        } else {
          sendPayPalConfirmationEvent(WalletsAnalytics.ACTION_CANCEL)
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
          sendPayPalConfirmationEvent(WalletsAnalytics.ACTION_CANCEL)
        else
          sendPayPalConfirmationEvent(WalletsAnalytics.ACTION_BUY)
      }
      view.successWebViewResult(data!!.data)
    }
  }

  private fun handleAuthenticationResult(resultCode: Int) {
    if (resultCode == AuthenticationPromptActivity.RESULT_OK) {
      view.authenticationResult(true)
    } else if (resultCode == AuthenticationPromptActivity.RESULT_CANCELED) {
      view.authenticationResult(false)
    }
  }
}