package com.asfoundation.wallet.topup

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.topup.address.BillingPaymentTopUpModel
import io.reactivex.Observable

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToAdyenPayment(paymentType: PaymentType, data: TopUpPaymentData)

  fun navigateToLocalPayment(paymentId: String, icon: String, label: String,
                             topUpData: TopUpPaymentData)

  fun navigateToBillingAddress(topUpData: TopUpPaymentData,
                               billingPaymentTopUpModel: BillingPaymentTopUpModel)

  fun finish(data: Bundle)

  fun finishActivity(data: Bundle)

  fun showBackupNotification(walletAddress: String)

  fun navigateBack()

  fun close(navigateToTransactions: Boolean = true)

  fun acceptResult(uri: Uri)

  fun showToolbar()

  fun lockOrientation()

  fun unlockRotation()

  fun cancelPayment()

  fun setFinishingPurchase()

  fun showError(@StringRes error: Int)

  fun getSupportClicks(): Observable<Any>

  fun showWalletValidation(@StringRes error: Int)

  fun getTryAgainClicks(): Observable<Any>

  fun popBackStack()

  fun launchPerkBonusService(address: String)
}
