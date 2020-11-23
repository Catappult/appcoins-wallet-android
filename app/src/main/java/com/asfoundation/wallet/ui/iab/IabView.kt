package com.asfoundation.wallet.ui.iab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.billing.adyen.PaymentType
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

  fun showOnChain(amount: BigDecimal, isBds: Boolean, bonus: String, gamificationLevel: Int)

  fun showAdyenPayment(amount: BigDecimal, currency: String?, isBds: Boolean,
                       paymentType: PaymentType, bonus: String?, isPreselected: Boolean,
                       iconUrl: String?, gamificationLevel: Int)

  fun showCarrierBilling(currency: String?, amount: BigDecimal,
                         bonus: BigDecimal, isPreselected: Boolean)

  fun showAppcoinsCreditsPayment(appcAmount: BigDecimal, gamificationLevel: Int)

  fun showLocalPayment(domain: String, skuId: String?, originalAmount: String?, currency: String?,
                       bonus: String?, selectedPaymentMethod: String, developerAddress: String,
                       type: String, amount: BigDecimal, callbackUrl: String?,
                       orderReference: String?, payload: String?, paymentMethodIconUrl: String,
                       paymentMethodLabel: String, gamificationLevel: Int)

  fun showPaymentMethodsView()

  fun showShareLinkPayment(domain: String, skuId: String?, originalAmount: String?,
                           originalCurrency: String?, amount: BigDecimal, type: String,
                           selectedPaymentMethod: String)

  fun showMergedAppcoins(fiatAmount: BigDecimal, currency: String, bonus: String,
                         isBds: Boolean, isDonation: Boolean, gamificationLevel: Int)

  fun showBillingAddress(value: BigDecimal, currency: String, bonus: String,
                         appcAmount: BigDecimal, targetFragment: Fragment, shouldStoreCard: Boolean,
                         isStored: Boolean)

  fun lockRotation()

  fun unlockRotation()

  fun showEarnAppcoins(domain: String, skuId: String?, amount: BigDecimal, type: String)

  fun launchIntent(intent: Intent)

  fun showUpdateRequiredView()

  fun finishActivity(data: Bundle)

  fun showBackupNotification(walletAddress: String)

  fun showWalletValidation(@StringRes error: Int)

  fun showError(@StringRes error: Int)

  fun getSupportClicks(): Observable<Any>

  fun errorDismisses(): Observable<Any>

  fun launchPerkBonusService(address: String)

  fun showAuthenticationActivity()

  fun onAuthenticationResult(): Observable<Boolean>

  fun backButtonPress(): Observable<Any>

  fun successWebViewResult(data: Uri?)

  fun authenticationResult(success: Boolean)
}
