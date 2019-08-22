package com.asfoundation.wallet.interact

import android.util.Pair
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.Levels
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.referrals.ReferralsScreen
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
                              private val referralInteractor: ReferralInteractorContract) {

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
    return Single.zip(referralInteractor.hasReferralUpdate(
        ReferralsScreen.PROMOTIONS),
        gamificationInteractor.hasNewLevel(GamificationScreen.PROMOTIONS),
        BiFunction { hasReferralUpdate: Boolean, hasNewLevel: Boolean ->
          hasReferralUpdate || hasNewLevel
        })
  }

  fun fetchTransactions(wallet: Wallet?): Observable<List<Transaction>> {
    return wallet?.let { fetchTransactionsInteract.fetch(wallet.address) } ?: Observable.just(
        emptyList())
  }

  fun stopTransactionFetch() {
    fetchTransactionsInteract.stop()
  }

  fun findWallet(): Single<Wallet> {
    return findDefaultWalletInteract.find()
  }
}
