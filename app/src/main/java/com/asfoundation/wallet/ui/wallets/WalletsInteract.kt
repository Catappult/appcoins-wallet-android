package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.entity.Wallet
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
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
import io.reactivex.schedulers.Schedulers

class WalletsInteract(private val balanceInteractor: BalanceInteractor,
                      private val fetchWalletsInteract: FetchWalletsInteract,
                      private val walletCreatorInteract: WalletCreatorInteract,
                      private val supportInteractor: SupportInteractor,
                      private val preferencesRepository: SharedPreferencesRepository,
                      private val gamificationRepository: Gamification,
                      private val logger: Logger,
                      private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase) {

  fun retrieveWalletsModel(): Single<WalletsModel> {
    lateinit var currentWallet: WalletBalance
    val wallets = ArrayList<WalletBalance>()
    val currentWalletAddress = preferencesRepository.getCurrentWalletAddress()
    return retrieveWallets().filter { it.isNotEmpty() }
        .flatMapCompletable { list ->
          Observable.fromIterable(list)
              .flatMapCompletable { wallet ->
                balanceInteractor.getTotalBalance(wallet.address)
                    .firstOrError()
                    .doOnSuccess { fiatValue ->
                      if (currentWalletAddress == wallet.address) {
                        currentWallet = WalletBalance(wallet.address, fiatValue,
                            currentWalletAddress == wallet.address)
                      }
                      wallets.add(WalletBalance(wallet.address, fiatValue,
                          currentWalletAddress == wallet.address))
                    }
                    .doOnError { logger.log("WalletsInteract", it) }
                    .ignoreElement()
              }
        }
        .toSingle {
          WalletsModel(getTotalBalance(currentWallet, wallets), wallets.size, currentWallet,
              wallets)
        }
  }

  fun getWalletsModel(): Single<WalletsModel> {
    lateinit var currentWallet: WalletBalance
    val wallets = ArrayList<WalletBalance>()
    val currentWalletAddress = preferencesRepository.getCurrentWalletAddress()
    return retrieveWallets().filter { it.isNotEmpty() }
        .flatMapCompletable { list ->
          Observable.fromIterable(list)
              .flatMapCompletable { wallet ->
                balanceInteractor.getTotalBalance(wallet.address)
                    .subscribeOn(Schedulers.io())
                    .firstOrError()
                    .doOnSuccess { fiatValue ->
                      if (currentWalletAddress == wallet.address) {
                        currentWallet = WalletBalance(wallet.address, fiatValue,
                            currentWalletAddress == wallet.address)
                      } else {
                        wallets.add(WalletBalance(wallet.address, fiatValue,
                            currentWalletAddress == wallet.address))
                      }
                    }
                    .doOnError { logger.log("WalletsInteract", it) }
                    .ignoreElement()
              }
        }
        .toSingle {
          val totalBalance = getTotalBalance(currentWallet, wallets)
          val balanceComparator = compareByDescending<WalletBalance> { it.balance.amount }
          val walletsSorted =
              wallets.sortedWith(balanceComparator.thenBy(WalletBalance::walletAddress))
          WalletsModel(totalBalance, wallets.size, currentWallet, walletsSorted)
        }
  }

  fun createWallet(): Completable {
    return walletCreatorInteract.create()
        .subscribeOn(Schedulers.io())
        .flatMapCompletable { wallet ->
          getCurrentPromoCodeUseCase()
              .flatMapCompletable { promoCode ->
                walletCreatorInteract.setDefaultWallet(wallet.address)
                    .andThen(gamificationRepository.getUserLevel(wallet.address, promoCode.code)
                        .doOnSuccess { supportInteractor.registerUser(it, wallet.address) }
                        .ignoreElement())
              }

        }
  }

  private fun getTotalBalance(currentWallet: WalletBalance,
                              walletBalance: List<WalletBalance>): FiatValue {
    val totalBalance = walletBalance.sumByBigDecimal { it.balance.amount }
    return FiatValue(totalBalance, currentWallet.balance.currency, currentWallet.balance.symbol)
  }

  private fun retrieveWallets(): Observable<List<Wallet>> {
    return fetchWalletsInteract.fetch()
        .subscribeOn(Schedulers.io())
        .map { it.toList() }
        .toObservable()
  }
}
