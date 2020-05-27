package com.asfoundation.wallet.ui.iab

import android.util.Pair
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class PaymentMethodsInteract(private val walletService: WalletService,
                             private val supportInteractor: SupportInteractor,
                             private val gamificationInteractor: GamificationInteractor,
                             private val balanceInteract: BalanceInteract,
                             private val walletBlockedInteract: WalletBlockedInteract,
                             private val inAppPurchaseInteractor: InAppPurchaseInteractor) {


  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable {
          Completable.fromAction {
            supportInteractor.registerUser(gamificationLevel, it.toLowerCase())
            supportInteractor.displayChatScreen()
          }
        }
  }

  fun getEthBalance(): Observable<Pair<Balance, FiatValue>> {
    return balanceInteract.getEthBalance()

  }

  fun getAppcBalance(): Observable<Pair<Balance, FiatValue>> {
    return balanceInteract.getAppcBalance()

  }

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> {
    return balanceInteract.getCreditsBalance()
  }

  fun isBonusActiveAndValid(): Boolean {
    return gamificationInteractor.isBonusActiveAndValid()
  }

  fun isBonusActiveAndValid(forecastBonus: ForecastBonusAndLevel): Boolean {
    return gamificationInteractor.isBonusActiveAndValid(forecastBonus)
  }

  fun getEarningBonus(packageName: String, amount: BigDecimal): Single<ForecastBonusAndLevel> {
    return gamificationInteractor.getEarningBonus(packageName, amount)
  }

  fun isWalletBlocked(): Single<Boolean> {
    return walletBlockedInteract.isWalletBlocked()
  }

  fun getCurrentPaymentStep(packageName: String, transactionBuilder: TransactionBuilder)
      : Single<AsfInAppPurchaseInteractor.CurrentPaymentStep> {
    return inAppPurchaseInteractor.getCurrentPaymentStep(packageName, transactionBuilder)
  }

  fun resume(uri: String?, transactionType: AsfInAppPurchaseInteractor.TransactionType?,
             packageName: String?, productName: String?, developerPayload: String?,
             isBds: Boolean): Completable? {
    return inAppPurchaseInteractor.resume(uri, transactionType, packageName, productName,
        developerPayload, isBds)
  }

  fun convertToLocalFiat(appcValue: Double): Single<FiatValue?> {
    return inAppPurchaseInteractor.convertToLocalFiat(appcValue)
  }

  fun hasAsyncLocalPayment(): Boolean {
    return inAppPurchaseInteractor.hasAsyncLocalPayment()
  }

  fun hasPreSelectedPaymentMethod(): Boolean {
    return inAppPurchaseInteractor.hasPreSelectedPaymentMethod()
  }

  fun removePreSelectedPaymentMethod() {
    return inAppPurchaseInteractor.removePreSelectedPaymentMethod()
  }

  fun removeAsyncLocalPayment() {
    return inAppPurchaseInteractor.removeAsyncLocalPayment()
  }

  fun getPaymentMethods(transaction: TransactionBuilder, transactionValue: String,
                        currency: String): Single<List<PaymentMethod>> {
    return inAppPurchaseInteractor.getPaymentMethods(transaction, transactionValue, currency)
  }

  fun mergeAppcoins(paymentMethods: List<PaymentMethod>): List<PaymentMethod> {
    return inAppPurchaseInteractor.mergeAppcoins(paymentMethods)
  }

  fun getPreSelectedPaymentMethod(): String {
    return inAppPurchaseInteractor.preSelectedPaymentMethod
  }

  fun getLastUsedPaymentMethod(): String {
    return inAppPurchaseInteractor.lastUsedPaymentMethod
  }
}