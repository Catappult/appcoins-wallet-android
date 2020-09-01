package com.asfoundation.wallet.interact

import android.util.Pair
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.Levels
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.PromotionsInteractorContract
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionDetails
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class TransactionViewInteract(private val findDefaultNetworkInteract: FindDefaultNetworkInteract,
                              private val findDefaultWalletInteract: FindDefaultWalletInteract,
                              private val fetchTransactionsInteract: FetchTransactionsInteract,
                              private val gamificationInteractor: GamificationInteractor,
                              private val balanceInteract: BalanceInteract,
                              private val promotionsInteractor: PromotionsInteractorContract,
                              private val cardNotificationsInteractor: CardNotificationsInteractor,
                              private val autoUpdateInteract: AutoUpdateInteract) {

  val levels: Single<Levels>
    get() = gamificationInteractor.getLevels()

  val appcBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteract.getAppcBalance()

  val ethereumBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteract.getEthBalance()

  val creditsBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteract.getCreditsBalance()

  val cardNotifications: Single<List<CardNotification>>
    get() = cardNotificationsInteractor.getCardNotifications()

  val userLevel: Single<Int>
    get() = gamificationInteractor.getUserStats()
        .map { it.level }

  fun findNetwork(): Single<NetworkInfo> {
    return findDefaultNetworkInteract.find()
  }

  fun hasPromotionUpdate(): Single<Boolean> {
    return promotionsInteractor.hasAnyPromotionUpdate(ReferralsScreen.PROMOTIONS,
        GamificationScreen.PROMOTIONS, PromotionUpdateScreen.PROMOTIONS)
  }

  fun fetchTransactions(wallet: Wallet?): Observable<List<Transaction>> {
    return wallet?.let { fetchTransactionsInteract.fetch(wallet.address) } ?: Observable.just(
        emptyList())
    //TODO
  }

  private fun addMockedSubscriptions(transactions: MutableList<Transaction>,
                                     it: Wallet): List<Transaction> {
    transactions.add(getActiveMockedTransaction(it.address))
    transactions.add(getExpiringMockedTransaction(it.address))
    transactions.add(getExpiredMockedTransaction(it.address))
    return transactions.toList()
  }

  private fun getActiveMockedTransaction(address: String): Transaction {
    return Transaction("0xd6d42df92b55be4b7d24c96c3dc546474ad638ff66cb061f2fd05e9b74e4e6a1",
        Transaction.TransactionType.SUBS, null, null, null, null, null, System.currentTimeMillis(),
        System.currentTimeMillis(), Transaction.TransactionStatus.SUCCESS, "10000000000000000000",
        address, "0x123c2124b7f2c18b502296ba884d9cde201f1c32",
        TransactionDetails("Real Boxing",
            TransactionDetails.Icon(TransactionDetails.Icon.Type.URL,
                "http://pool.img.aptoide.com/bds-store/59a7b62a169a832e96dbd7df82d6e3cc_icon.png"),
            "Subscription"), "EUR", emptyList())
  }

  private fun getExpiringMockedTransaction(address: String): Transaction {
    return Transaction("0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9-2",
        Transaction.TransactionType.SUBS, null, null, null, null, null, System.currentTimeMillis(),
        System.currentTimeMillis(), Transaction.TransactionStatus.SUCCESS, "10000000000000000000",
        address, "0x123c2124b7f2c18b502296ba884d9cde201f1c32",
        TransactionDetails("Cuties",
            TransactionDetails.Icon(TransactionDetails.Icon.Type.URL,
                "http://pool.img.aptoide.com/bds-store/d6267357ec641dd583a0ad318fa0741b_icon.png"),
            "Subscription"), "EUR", emptyList())
  }

  private fun getExpiredMockedTransaction(address: String): Transaction {
    return Transaction("0xa03f872318ee763e7cd92923304671e0115f883c32c0520ca3b7c3a1a9d47f98",
        Transaction.TransactionType.SUBS, null, null, null, null, null, System.currentTimeMillis(),
        System.currentTimeMillis(), Transaction.TransactionStatus.SUCCESS, "12200000000000000000",
        address, "0x123c2124b7f2c18b502296ba884d9cde201f1c32",
        TransactionDetails("Creative Destruction",
            TransactionDetails.Icon(TransactionDetails.Icon.Type.URL,
                "http://pool.img.aptoide.com/bds-store/fc1a8567262637b89f4f8bd9f0c69559_icon.jpg"),
            "Subscription"), "EUR", emptyList())
  }

  fun stopTransactionFetch() = fetchTransactionsInteract.stop()

  fun findWallet(): Single<Wallet> {
    return findDefaultWalletInteract.find()
  }

  fun dismissNotification(cardNotification: CardNotification): Completable {
    return cardNotificationsInteractor.dismissNotification(cardNotification)
  }

  fun retrieveUpdateIntent() = autoUpdateInteract.buildUpdateIntent()
}
