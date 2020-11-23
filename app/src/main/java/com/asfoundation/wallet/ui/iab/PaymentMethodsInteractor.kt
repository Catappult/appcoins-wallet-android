package com.asfoundation.wallet.ui.iab

import android.util.Pair
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.PendingTransaction
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.repository.BdsPendingTransactionService
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal

class PaymentMethodsInteractor(private val supportInteractor: SupportInteractor,
                               private val gamificationInteractor: GamificationInteractor,
                               private val balanceInteractor: BalanceInteractor,
                               private val walletBlockedInteract: WalletBlockedInteract,
                               private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                               private val preferencesRepositoryType: PreferencesRepositoryType,
                               private val billing: Billing,
                               private val bdsPendingTransactionService: BdsPendingTransactionService) {


  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun getEthBalance(): Observable<Pair<Balance, FiatValue>> = balanceInteractor.getEthBalance()

  fun getAppcBalance(): Observable<Pair<Balance, FiatValue>> = balanceInteractor.getAppcBalance()

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> =
      balanceInteractor.getCreditsBalance()

  fun isBonusActiveAndValid() = gamificationInteractor.isBonusActiveAndValid()

  fun isBonusActiveAndValid(forecastBonus: ForecastBonusAndLevel) =
      gamificationInteractor.isBonusActiveAndValid(forecastBonus)

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonusAndLevel> =
      gamificationInteractor.getEarningBonus(packageName, amount)

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun getCurrentPaymentStep(packageName: String, transactionBuilder: TransactionBuilder)
      : Single<AsfInAppPurchaseInteractor.CurrentPaymentStep> =
      inAppPurchaseInteractor.getCurrentPaymentStep(packageName, transactionBuilder)

  fun resume(uri: String?, transactionType: AsfInAppPurchaseInteractor.TransactionType,
             packageName: String, productName: String?, developerPayload: String?,
             isBds: Boolean): Completable =
      inAppPurchaseInteractor.resume(uri, transactionType, packageName, productName,
          developerPayload, isBds)

  fun convertToLocalFiat(appcValue: Double): Single<FiatValue> =
      inAppPurchaseInteractor.convertToLocalFiat(appcValue)

  fun hasAsyncLocalPayment() = inAppPurchaseInteractor.hasAsyncLocalPayment()

  fun hasPreSelectedPaymentMethod() = inAppPurchaseInteractor.hasPreSelectedPaymentMethod()

  fun removePreSelectedPaymentMethod() = inAppPurchaseInteractor.removePreSelectedPaymentMethod()

  fun removeAsyncLocalPayment() = inAppPurchaseInteractor.removeAsyncLocalPayment()

  fun getPaymentMethods(transaction: TransactionBuilder, transactionValue: String,
                        currency: String): Single<List<PaymentMethod>> =
      inAppPurchaseInteractor.getPaymentMethods(transaction, transactionValue, currency)

  fun mergeAppcoins(paymentMethods: List<PaymentMethod>): List<PaymentMethod> =
      inAppPurchaseInteractor.mergeAppcoins(paymentMethods)

  fun swapDisabledPositions(paymentMethods: List<PaymentMethod>): List<PaymentMethod> =
      inAppPurchaseInteractor.swapDisabledPositions(paymentMethods)

  fun getPreSelectedPaymentMethod(): String = inAppPurchaseInteractor.preSelectedPaymentMethod

  fun getLastUsedPaymentMethod(): String = inAppPurchaseInteractor.lastUsedPaymentMethod

  fun hasAuthenticationPermission() = preferencesRepositoryType.hasAuthenticationPermission()

  fun checkTransactionStateFromTransactionId(uid: String): Observable<PendingTransaction> =
      bdsPendingTransactionService.checkTransactionStateFromTransactionId(uid)

  fun getSkuTransaction(appPackage: String, skuId: String?, networkThread: Scheduler) =
      billing.getSkuTransaction(appPackage, skuId, networkThread)

  fun getSkuPurchase(appPackage: String, skuId: String?, networkThread: Scheduler) =
      billing.getSkuPurchase(appPackage, skuId, networkThread)

  fun getPurchases(appPackage: String, inapp: BillingSupportedType, networkThread: Scheduler) =
      billing.getPurchases(appPackage, inapp, networkThread)
}