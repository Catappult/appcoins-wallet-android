package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor.CurrentPaymentStep
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*

class OnChainBuyInteract(private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                         private val supportInteractor: SupportInteractor,
                         private val walletService: WalletService,
                         private val walletBlockedInteract: WalletBlockedInteract,
                         private val smsValidationInteract: SmsValidationInteract) {

  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable {
          Completable.fromAction {
            supportInteractor.registerUser(gamificationLevel, it.toLowerCase(Locale.ROOT))
            supportInteractor.displayChatScreen()
          }
        }
  }

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun isWalletVerified() =
      walletService.getWalletAddress()
          .flatMap { smsValidationInteract.isValidated(it) }
          .onErrorReturn { true }

  fun getTransactionState(uri: String?): Observable<Payment> =
      inAppPurchaseInteractor.getTransactionState(uri)

  fun send(uri: String?, transactionType: AsfInAppPurchaseInteractor.TransactionType,
           packageName: String, productName: String?, developerPayload: String?,
           isBds: Boolean): Completable {
    return inAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
        developerPayload, isBds)
  }

  fun parseTransaction(uri: String?, isBds: Boolean): Single<TransactionBuilder> =
      inAppPurchaseInteractor.parseTransaction(uri, isBds)

  fun getCurrentPaymentStep(packageName: String,
                            transactionBuilder: TransactionBuilder): Single<CurrentPaymentStep> =
      inAppPurchaseInteractor.getCurrentPaymentStep(packageName, transactionBuilder)

  fun resume(uri: String?, transactionType: AsfInAppPurchaseInteractor.TransactionType,
             packageName: String, productName: String?, developerPayload: String?,
             isBds: Boolean, type: String): Completable {
    return inAppPurchaseInteractor.resume(uri, transactionType, packageName, productName,
        developerPayload, isBds, type)
  }

  fun getCompletedPurchase(transaction: Payment, isBds: Boolean): Single<Payment> =
      inAppPurchaseInteractor.getCompletedPurchase(transaction, isBds)

  fun remove(uri: String?): Completable = inAppPurchaseInteractor.remove(uri)

  fun getTopUpChannelSuggestionValues(price: BigDecimal): List<BigDecimal> =
      inAppPurchaseInteractor.getTopUpChannelSuggestionValues(price)

  fun convertToFiat(appcValue: Double, currency: String): Single<FiatValue> =
      inAppPurchaseInteractor.convertToFiat(appcValue, currency)

  fun getBillingMessagesMapper(): BillingMessagesMapper =
      inAppPurchaseInteractor.billingMessagesMapper

  fun getWalletAddress() = walletService.getWalletAddress()
}