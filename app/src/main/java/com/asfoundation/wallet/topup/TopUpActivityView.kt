package com.asfoundation.wallet.topup

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.billing.adyen.PaymentType
import io.reactivex.Observable

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToAdyenPayment(paymentType: PaymentType, data: TopUpPaymentData)

  fun navigateToPaypalV2(paymentType: PaymentType, data: TopUpPaymentData)

  fun navigateToLocalPayment(paymentId: String, icon: String, label: String, async: Boolean,
                             topUpData: TopUpPaymentData)

  fun navigateToBillingAddress(topUpData: TopUpPaymentData, fiatAmount: String,
                               fiatCurrency: String, targetFragment: Fragment,
                               shouldStoreCard: Boolean, preSelected: Boolean)

  fun finish(data: Bundle)

  fun finishActivity(data: Bundle)

  fun showBackupNotification(walletAddress: String)

  fun close(navigateToTransactions: Boolean = true)

  fun acceptResult(uri: Uri)

  fun lockOrientation()

  fun unlockRotation()

  fun cancelPayment()

  fun setFinishingPurchase(value: Boolean)

  fun showError(@StringRes error: Int)

  fun getSupportClicks(): Observable<Any>

  fun showVerification()

  fun getTryAgainClicks(): Observable<Any>

  fun popBackStack()

  fun launchPerkBonusAndGamificationService(address: String)

  fun createChallengeReward()
}
