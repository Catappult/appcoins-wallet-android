package com.asfoundation.wallet.ui.iab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.R
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asfoundation.wallet.promotions.usecases.StartVipReferralPollingUseCase
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.update_required.use_cases.GetAutoUpdateModelUseCase
import com.asfoundation.wallet.update_required.use_cases.HasRequiredHardUpdateUseCase
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class IabPresenter(
  private val view: IabView,
  private val networkScheduler: Scheduler,
  private val viewScheduler: Scheduler,
  private val disposable: CompositeDisposable,
  private val billingAnalytics: BillingAnalytics,
  private val iabInteract: IabInteract,
  private val getAutoUpdateModelUseCase: GetAutoUpdateModelUseCase,
  private val hasRequiredHardUpdateUseCase: HasRequiredHardUpdateUseCase,
  private val startVipReferralPollingUseCase: StartVipReferralPollingUseCase,
  private val logger: Logger,
  private val transaction: TransactionBuilder?,
  private val errorFromReceiver: String? = null
) {

  private var firstImpression = true
  var webViewResultCode: String? = null

  companion object {
    private val TAG = IabActivity::class.java.name
    private const val FIRST_IMPRESSION = "first_impression"
  }

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      firstImpression = it.getBoolean(FIRST_IMPRESSION, firstImpression)
    }
    if (errorFromReceiver != null) {
      view.showError(R.string.purchase_error_connection_issue)
      return
    }
    if (savedInstanceState == null) {
      handlePurchaseStartAnalytics(transaction)
      view.showPaymentMethodsView()
    }
  }

  fun onResume() {
    handleAutoUpdate()
    handleSupportClicks()
    handleErrorDismisses()
  }

  private fun handleErrorDismisses() {
    disposable.add(view.errorDismisses()
      .doOnNext { view.close(Bundle()) }
      .subscribe({ }, { view.close(Bundle()) })
    )
  }

  private fun handleSupportClicks() {
    disposable.add(view.getSupportClicks()
      .throttleFirst(50, TimeUnit.MILLISECONDS)
      .observeOn(viewScheduler)
      .flatMapCompletable { iabInteract.showSupport() }
      .subscribe({}, { it.printStackTrace() })
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
      .flatMap {
        startVipReferralPollingUseCase(Wallet(it)).toSingleDefault(it)
      }
      .doOnSuccess {
        view.launchPerkBonusAndGamificationService(it)
        view.finishActivity(bundle)
      }
      .doOnError { view.finishActivity(bundle) }
      .subscribe({}, { it.printStackTrace() })
    )
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
          billingAnalytics.sendPurchaseStartEvent(
            transaction?.domain, transaction?.skuId,
            transaction?.amount()
              .toString(), iabInteract.getPreSelectedPaymentMethod(),
            transaction?.type, BillingAnalytics.WALLET_PRESELECTED_PAYMENT_METHOD
          )
        } else {
          billingAnalytics.sendPurchaseStartWithoutDetailsEvent(
            transaction?.domain,
            transaction?.skuId, transaction?.amount()
              .toString(), transaction?.type,
            BillingAnalytics.WALLET_PAYMENT_METHOD
          )
        }
        firstImpression = false
      }
    }
      .subscribeOn(networkScheduler)
      .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleAutoUpdate() {
    disposable.add(getAutoUpdateModelUseCase()
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .filter {
        hasRequiredHardUpdateUseCase(it.blackList, it.updateVersionCode, it.updateMinSdk)
      }
      .doOnSuccess { view.showUpdateRequiredView() }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposable.clear()

  fun onSaveInstance(outState: Bundle) {
    outState.putBoolean(FIRST_IMPRESSION, firstImpression)
  }

  fun savePreselectedPaymentMethod(bundle: Bundle) {
    bundle.getString(PRE_SELECTED_PAYMENT_METHOD_KEY)
      ?.let { iabInteract.savePreSelectedPaymentMethod(it) }
  }

  private fun sendPayPalConfirmationEvent(action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transaction?.domain, transaction?.skuId,
      transaction?.amount()
        .toString(), "paypal",
      transaction?.type, action
    )
  }

  private fun sendCarrierBillingConfirmationEvent(action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transaction?.domain, transaction?.skuId,
      transaction?.amount()
        .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
      transaction?.type, action
    )
  }

  private fun sendPaypalUrlEvent(data: Intent) {
    val amountString = transaction?.amount()
      .toString()
    billingAnalytics.sendPaypalUrlEvent(
      transaction?.domain, transaction?.skuId,
      amountString, "PAYPAL", getQueryParameter(data, "type"),
      getQueryParameter(data, "resultCode"), data.dataString
    )
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
    when (resultCode) {
      WebViewActivity.FAIL -> {
        if (data?.dataString?.contains("codapayments") != true) {
          if (data?.dataString?.contains(
              BillingWebViewFragment.CARRIER_BILLING_ONE_BIP_SCHEMA
            ) == true
          ) {
            sendCarrierBillingConfirmationEvent(BillingAnalytics.ACTION_CANCEL)
          } else {
            sendPayPalConfirmationEvent(BillingAnalytics.ACTION_CANCEL)
          }
        }
        if (data?.dataString?.contains(BillingWebViewFragment.OPEN_SUPPORT) == true) {
          logger.log(TAG, Exception("WebViewResult ${data.dataString}"))
          iabInteract.showSupport().subscribe({}, { it.printStackTrace() })
        }
        view.showPaymentMethodsView()
      }
      WebViewActivity.SUCCESS -> {
        if (data?.scheme?.contains("adyencheckout") == true) {
          sendPaypalUrlEvent(data)
          sendPayPalConfirmationEvent(BillingAnalytics.ACTION_BUY)
        }
        view.webViewResultCode = data?.let { getQueryParameter(it, "resultCode") }
        view.successWebViewResult(data!!.data)
      }
      WebViewActivity.USER_CANCEL -> {
        if (data?.scheme?.contains("adyencheckout") == true) {
          sendPaypalUrlEvent(data)
          sendPayPalConfirmationEvent(BillingAnalytics.ACTION_CANCEL)
        }
        view.showPaymentMethodsView()
      }
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