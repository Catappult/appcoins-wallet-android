package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.appcoins.rewards.ErrorInfo
import com.appcoins.wallet.appcoins.rewards.ErrorMapper
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.entity.PendingTransaction
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.repository.BdsPendingTransactionService
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.appcoins.wallet.sharedpreferences.FingerprintPreferencesDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class PaymentMethodsInteractor @Inject constructor(
  private val supportInteractor: SupportInteractor,
  private val gamificationInteractor: GamificationInteractor,
  private val walletBlockedInteract: WalletBlockedInteract,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val fingerprintPreferences: FingerprintPreferencesDataSource,
  private val billing: Billing,
  private val errorMapper: ErrorMapper,
  private val bdsPendingTransactionService: BdsPendingTransactionService,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {


  fun showSupport(gamificationLevel: Int): Completable {
    return supportInteractor.showSupport(gamificationLevel)
  }

  fun isBonusActiveAndValid() = gamificationInteractor.isBonusActiveAndValid()

  fun isBonusActiveAndValid(forecastBonus: ForecastBonusAndLevel) =
      gamificationInteractor.isBonusActiveAndValid(forecastBonus)

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonusAndLevel> {
    return getCurrentPromoCodeUseCase()
        .flatMap {
          gamificationInteractor.getEarningBonus(packageName, amount, it.code)
        }
  }


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

  fun getSkuTransaction(appPackage: String, skuId: String?,
                        networkThread: Scheduler, type: BillingSupportedType) =
      billing.getSkuTransaction(appPackage, skuId, networkThread, type)

  fun getSkuPurchase(appPackage: String, skuId: String?, purchaseUid: String?, type: String,
                     orderReference: String?,
                     hash: String?, networkThread: Scheduler): Single<PurchaseBundleModel> {
    return inAppPurchaseInteractor.getCompletedPurchaseBundle(type, appPackage, skuId, purchaseUid,
        orderReference, hash, networkThread)
  }

  fun getSkuDetails(domain: String, sku: String, type: BillingSupportedType): Single<Product> {
    return billing.getProducts(domain, mutableListOf(sku), type)
        .map { products -> products.first() }
  }

  fun getPurchases(appPackage: String, inapp: BillingSupportedType, networkThread: Scheduler) =
      billing.getPurchases(appPackage, inapp, networkThread)

  fun isAbleToSubscribe(packageName: String, skuId: String,
                        networkThread: Scheduler): Single<SubscriptionStatus> {
    return billing.getSubscriptionToken(packageName, skuId, networkThread)
        .map { SubscriptionStatus(true) }
        .onErrorReturn {
          val errorInfo = errorMapper.map(it)
          val isAlreadySubscribed = errorInfo.errorType == ErrorInfo.ErrorType.SUB_ALREADY_OWNED
          SubscriptionStatus(false, isAlreadySubscribed)
        }
  }
}