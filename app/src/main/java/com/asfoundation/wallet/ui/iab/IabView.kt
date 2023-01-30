package com.asfoundation.wallet.ui.iab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Observable
import java.math.BigDecimal

/**
 * Created by franciscocalado on 20/07/2018.
 */

interface IabView {

  fun disableBack()

  fun enableBack()

  fun finish(bundle: Bundle)

  fun finishWithError()

  fun navigateBack()

  fun close(bundle: Bundle?)

  fun navigateToWebViewAuthorization(url: String)

  fun showOnChain(
    amount: BigDecimal,
    isBds: Boolean,
    bonus: String,
    gamificationLevel: Int,
    transactionBuilder: TransactionBuilder
  )

  fun showAdyenPayment(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    paymentType: PaymentType,
    bonus: String?,
    isPreselected: Boolean,
    iconUrl: String?,
    gamificationLevel: Int,
    isSubscription: Boolean,
    frequency: String?
  )

  fun showPayPalV2(
    amount: BigDecimal,
    currency: String?,
    isBds: Boolean,
    paymentType: PaymentType,
    bonus: String?,
    isPreselected: Boolean,
    iconUrl: String?,
    gamificationLevel: Int,
    isSubscription: Boolean,
    frequency: String?
  )

  fun showCarrierBilling(
    currency: String?,
    amount: BigDecimal,
    bonus: BigDecimal?,
    isPreselected: Boolean
  )

  fun showAppcoinsCreditsPayment(
    appcAmount: BigDecimal,
    isPreselected: Boolean,
    gamificationLevel: Int,
    transactionBuilder: TransactionBuilder
  )

  fun showLocalPayment(
    domain: String,
    skuId: String?,
    originalAmount: String?,
    currency: String?,
    bonus: String?,
    selectedPaymentMethod: String,
    developerAddress: String,
    type: String,
    amount: BigDecimal,
    callbackUrl: String?,
    orderReference: String?,
    payload: String?,
    origin: String?,
    paymentMethodIconUrl: String,
    paymentMethodLabel: String,
    async: Boolean,
    referralUrl: String?,
    gamificationLevel: Int
  )

  fun showPaymentMethodsView()

  fun showShareLinkPayment(
    domain: String,
    skuId: String?,
    originalAmount: String?,
    originalCurrency: String?,
    amount: BigDecimal,
    type: String,
    selectedPaymentMethod: String
  )

  fun showMergedAppcoins(
    fiatAmount: BigDecimal,
    currency: String,
    bonus: String,
    isBds: Boolean,
    isDonation: Boolean,
    gamificationLevel: Int,
    transaction: TransactionBuilder,
    isSubscription: Boolean,
    frequency: String?
  )

  fun showBillingAddress(
    value: BigDecimal,
    currency: String,
    bonus: String,
    appcAmount: BigDecimal,
    targetFragment: Fragment,
    shouldStoreCard: Boolean,
    isStored: Boolean
  )

  fun lockRotation()

  fun unlockRotation()

  fun showEarnAppcoins(domain: String, skuId: String?, amount: BigDecimal, type: String)

  fun launchIntent(intent: Intent)

  fun showUpdateRequiredView()

  fun finishActivity(data: Bundle)

  fun showBackupNotification(walletAddress: String)

  fun showVerification(isWalletVerified: Boolean)

  fun showError(@StringRes error: Int)

  fun getSupportClicks(): Observable<Any>

  fun errorDismisses(): Observable<Any>

  fun launchPerkBonusAndGamificationService(address: String)

  fun showAuthenticationActivity()

  fun onAuthenticationResult(): Observable<Boolean>

  fun backButtonPress(): Observable<Any>

  fun successWebViewResult(data: Uri?)

  fun authenticationResult(success: Boolean)

  fun showTopupFlow()
}
