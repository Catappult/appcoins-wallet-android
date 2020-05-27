package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor.CurrentPaymentStep
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class OnChainBuyInteract(private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                         private val supportInteractor: SupportInteractor,
                         private val walletService: WalletService) {

  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable {
          Completable.fromAction {
            supportInteractor.registerUser(gamificationLevel, it.toLowerCase())
            supportInteractor.displayChatScreen()
          }
        }
  }

  fun getTransactionState(uri: String?): Observable<Payment> {
    return inAppPurchaseInteractor.getTransactionState(uri)
  }

  fun send(uri: String, transactionType: AsfInAppPurchaseInteractor.TransactionType,
           packageName: String, productName: String, developerPayload: String,
           isBds: Boolean): Completable {
    return inAppPurchaseInteractor.send(uri, transactionType, packageName, productName,
        developerPayload, isBds)
  }

  fun parseTransaction(uri: String, isBds: Boolean): Single<TransactionBuilder> {
    return inAppPurchaseInteractor.parseTransaction(uri, isBds)
  }

  fun getCurrentPaymentStep(packageName: String,
                            transactionBuilder: TransactionBuilder): Single<CurrentPaymentStep> {
    return inAppPurchaseInteractor.getCurrentPaymentStep(packageName, transactionBuilder)
  }

  fun resume(uri: String, transactionType: AsfInAppPurchaseInteractor.TransactionType,
             packageName: String, productName: String, developerPayload: String,
             isBds: Boolean): Completable {
    return inAppPurchaseInteractor.resume(uri, transactionType, packageName, productName,
        developerPayload, isBds)
  }

  fun getCompletedPurchase(transaction: Payment, isBds: Boolean): Single<Payment> {
    return inAppPurchaseInteractor.getCompletedPurchase(transaction, isBds)
  }

  fun remove(uri: String): Completable {
    return inAppPurchaseInteractor.remove(uri)
  }

  fun getTopUpChannelSuggestionValues(price: BigDecimal): List<BigDecimal> {
    return inAppPurchaseInteractor.getTopUpChannelSuggestionValues(price)
  }

  fun convertToFiat(appcValue: Double, currency: String): Single<FiatValue> {
    return inAppPurchaseInteractor.convertToFiat(appcValue, currency)
  }

  fun getBillingMessagesMapper(): BillingMessagesMapper {
    return inAppPurchaseInteractor.billingMessagesMapper
  }
}