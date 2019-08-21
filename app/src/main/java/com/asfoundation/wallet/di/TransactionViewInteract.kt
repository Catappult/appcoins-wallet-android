package com.asfoundation.wallet.di

import android.util.Pair
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.Levels
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FetchTransactionsInteract
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class TransactionViewInteract(private val findDefaultNetworkInteract: FindDefaultNetworkInteract,
                              private val findDefaultWalletInteract: FindDefaultWalletInteract,
                              private val fetchTransactionsInteract: FetchTransactionsInteract,
                              private val gamificationInteractor: GamificationInteractor,
                              private val balanceInteract: BalanceInteract,
                              private val referralInteractor: ReferralTestInteractor) {

  val levels: Single<Levels>
    get() = gamificationInteractor.getLevels()

  val tokenBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteract.getAppcBalance()

  val ethereumBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteract.getEthBalance()

  val creditsBalance: Observable<Pair<Balance, FiatValue>>
    get() = balanceInteract.getCreditsBalance()

  fun findNetwork(): Single<NetworkInfo> {
    return findDefaultNetworkInteract.find()
  }

  fun hasPromotionUpdate(): Single<Boolean> {
    return Single.zip(referralInteractor.hasReferralUpdate(),
        gamificationInteractor.hasNewLevel(GamificationScreen.PROMOTIONS),
        BiFunction { hasReferralUpdate: Boolean, hasNewLevel: Boolean ->
          hasReferralUpdate || hasNewLevel
        })
  }

  fun fetchTransactions(wallet: Wallet?): Observable<List<Transaction>> {
    return if (wallet != null) {
      fetchTransactionsInteract.fetch(wallet.address)
    } else {
      Observable.just(emptyList())
    }
  }

  fun stopTransactionFetch() {
    fetchTransactionsInteract.stop()
  }

  fun findWallet(): Single<Wallet> {
    return findDefaultWalletInteract.find()
  }
}
