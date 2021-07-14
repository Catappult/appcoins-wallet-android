package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.sumByBigDecimal
import com.asfoundation.wallet.wallets.FetchWalletsInteract
import com.asfoundation.wallet.wallets.WalletCreatorInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class WalletsInteract(private val balanceInteractor: BalanceInteractor,
                      private val fetchWalletsInteract: FetchWalletsInteract,
                      private val walletCreatorInteract: WalletCreatorInteract,
                      private val supportInteractor: SupportInteractor,
                      private val preferencesRepository: SharedPreferencesRepository,
                      private val gamificationRepository: Gamification,
                      private val logger: Logger) {

  fun retrieveWalletsModel(): Single<WalletsModel> {
    val wallets = ArrayList<WalletBalance>()
    val currentWalletAddress = preferencesRepository.getCurrentWalletAddress()
    return retrieveWallets().filter { it.isNotEmpty() }
        .flatMapCompletable { list ->
          Observable.fromIterable(list)
              .flatMapCompletable { wallet ->
                balanceInteractor.getTotalBalance(wallet.address)
                    .firstOrError()
                    .doOnSuccess { fiatValue ->
                      wallets.add(WalletBalance(wallet.address, fiatValue,
                          currentWalletAddress == wallet.address))
                    }
                    .doOnError { logger.log("WalletsInteract", it) }
                    .ignoreElement()
              }
        }
        .toSingle {
          WalletsModel(getTotalBalance(wallets), wallets.size, wallets)
        }
  }

  fun createWallet(): Completable {
    return walletCreatorInteract.create()
        .flatMapCompletable { wallet ->
          walletCreatorInteract.setDefaultWallet(wallet.address)
              .andThen(gamificationRepository.getUserLevel(wallet.address)
                  .doOnSuccess { supportInteractor.registerUser(it, wallet.address) }
                  .ignoreElement())
        }
  }

  private fun getTotalBalance(walletBalance: List<WalletBalance>): FiatValue {
    val totalBalance = walletBalance.sumByBigDecimal { it.balance.amount }
    return FiatValue(totalBalance, walletBalance[0].balance.currency,
        walletBalance[0].balance.symbol)
  }

  private fun retrieveWallets(): Observable<List<Wallet>> {
    return fetchWalletsInteract.fetch()
        .map { it.toList() }
        .toObservable()
  }
}
