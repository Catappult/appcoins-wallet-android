package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import android.util.Pair
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.PendingTransaction
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.repository.BdsPendingTransactionService
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
                               private val fingerprintPreferences: FingerprintPreferencesRepositoryContract,
                               private val billing: Billing,
                               private val billingMessagesMapper: BillingMessagesMapper,
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
             isBds: Boolean, type: String, transaction: TransactionBuilder): Completable =
      inAppPurchaseInteractor.resume(uri, transactionType, packageName, productName,
          developerPayload, isBds, type, transaction)

  fun convertAppcToLocalFiat(appcValue: Double): Single<FiatValue> =
      inAppPurchaseInteractor.convertToLocalFiat(appcValue)

  fun convertCurrencyToLocalFiat(value: Double, currency: String): Single<FiatValue> =
    inAppPurchaseInteractor.convertFiatToLocalFiat(value, currency)

  fun convertCurrencyToAppc(value: Double, currency: String): Single<FiatValue> =
    inAppPurchaseInteractor.convertFiatToAppc(value, currency)


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

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()

  fun checkTransactionStateFromTransactionId(uid: String): Observable<PendingTransaction> =
      bdsPendingTransactionService.checkTransactionStateFromTransactionId(uid)

  fun getSkuTransaction(appPackage: String, skuId: String?, transactionType: String,
                        networkThread: Scheduler) =
      billing.getSkuTransaction(appPackage, skuId, transactionType, networkThread)

  fun getSkuPurchase(appPackage: String, skuId: String?, type: String, orderReference: String?,
                     hash: String?, networkThread: Scheduler): Single<Bundle> {
    return if (isInApp(type) && skuId != null) {
      billing.getSkuPurchase(appPackage, skuId, networkThread)
          .map { billingMessagesMapper.mapPurchase(it, orderReference) }
    } else {
      Single.just(billingMessagesMapper.successBundle(hash))
    }
  }

  fun getSkuDetails(domain: String, sku: String): Single<Product> {
    return billing.getProducts(domain, mutableListOf(sku)).map { products -> products.first() }
  }

  private fun isInApp(type: String) =
      type.equals(INAPP_TRANSACTION_TYPE, ignoreCase = true)

  fun getPurchases(appPackage: String, inapp: BillingSupportedType, networkThread: Scheduler) =
      billing.getPurchases(appPackage, inapp, networkThread)


  private companion object {
    private const val INAPP_TRANSACTION_TYPE = "INAPP"
  }
}