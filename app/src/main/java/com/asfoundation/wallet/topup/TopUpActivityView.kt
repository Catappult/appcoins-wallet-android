package com.asfoundation.wallet.topup

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import com.asfoundation.wallet.billing.adyen.PaymentType
import io.reactivex.Observable
import java.math.BigDecimal

interface TopUpActivityView {
  fun showTopUpScreen()

  fun navigateToPayment(paymentType: PaymentType,
                        data: TopUpData,
                        selectedCurrency: String,
                        transactionType: String, bonusValue: BigDecimal,
                        gamificationLevel: Int, bonusSymbol: String)

  fun finish(data: Bundle)

  fun finishActivity(data: Bundle)

  fun showBackupNotification(walletAddress: String)

  fun navigateBack()

  fun close(navigateToTransactions: Boolean)

  fun acceptResult(uri: Uri)

  fun showToolbar()

  fun lockOrientation()

  fun unlockRotation()

  fun cancelPayment()

  fun setFinishingPurchase()

  fun showError(@StringRes error: Int)

  fun getSupportClicks(): Observable<Any>

  fun showWalletValidation(@StringRes error: Int)
}
