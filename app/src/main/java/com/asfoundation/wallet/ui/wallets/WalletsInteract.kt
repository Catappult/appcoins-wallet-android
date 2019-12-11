package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.interact.FetchWalletsInteract
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class WalletsInteract(private val balanceInteract: BalanceInteract,
                      private val fetchWalletsInteract: FetchWalletsInteract,
                      private val createWalletInteract: CreateWalletInteract,
                      private val preferencesRepository: SharedPreferencesRepository) {

  fun retrieveWalletsModel(): Single<WalletsModel> {
    val walletsBalance = ArrayList<WalletBalance>()
    val currentWalletAddress = preferencesRepository.getCurrentWalletAddress()
    return retrieveWallets().filter { it.isNotEmpty() }
        .flatMapCompletable { list ->
          Observable.fromIterable(list)
              .flatMapCompletable { wallet ->
                balanceInteract.getTotalBalance(wallet)
                    .take(1)
                    .flatMapCompletable { fiatValue ->
                      Completable.fromAction {
                        walletsBalance.add(WalletBalance(wallet.address, fiatValue,
                            currentWalletAddress == wallet.address))
                      }
                    }
              }
        }
        .toSingle {
          WalletsModel(getTotalBalance(walletsBalance), walletsBalance.size, walletsBalance)
        }
  }

  fun createWallet(): Completable {
    return createWalletInteract.create()
        .flatMapCompletable {
          Completable.fromAction {
            createWalletInteract.setDefaultWallet(it.address)
          }
        }
  }

  private fun getTotalBalance(walletBalance: MutableList<WalletBalance>): FiatValue {
    var totalBalance = BigDecimal.ZERO
    for (wallet in walletBalance) totalBalance = totalBalance.add(wallet.balance.amount)
    return FiatValue(totalBalance, walletBalance.elementAt(0).balance.currency,
        walletBalance.elementAt(0).balance.symbol)
  }

  private fun retrieveWallets(): Observable<MutableList<Wallet>> {
    return fetchWalletsInteract.fetch()
        .map { it.toMutableList() }
        .toObservable()
  }
}
