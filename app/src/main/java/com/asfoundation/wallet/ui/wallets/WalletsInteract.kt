package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.sumByBigDecimal
import com.asfoundation.wallet.wallets.FetchWalletsInteract
import com.asfoundation.wallet.wallets.WalletCreatorInteract
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WalletsInteract @Inject constructor(
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val fetchWalletsInteract: FetchWalletsInteract,
  private val walletCreatorInteract: WalletCreatorInteract,
  private val supportInteractor: SupportInteractor,
  private val preferencesRepository: SharedPreferencesRepository,
  private val gamificationRepository: Gamification,
  private val logger: Logger,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {

  fun observeWalletsModel(): Observable<WalletsModel> =
    retrieveWallets().filter { it.isNotEmpty() }
      .flatMapIterable { it }
      .flatMap { observeWalletInfoUseCase(it.address, update = true, updateFiat = false) }
      .map {
        listOf(
          WalletBalance(
            it.name,
            it.wallet,
            it.walletBalance.overallFiat,
            preferencesRepository.getCurrentWalletAddress() == it.wallet,
            it.backupDate
          )
        )
      }
      .doOnError { logger.log("WalletsInteract", it) }
      .scan { list, itemList ->
        list.filter { it.walletAddress != itemList[0].walletAddress } + itemList
      }
      .throttleLast(100, TimeUnit.MILLISECONDS)
      .map { list ->
        val totalBalance = getTotalBalance(list)
        val balanceComparator = compareByDescending<WalletBalance> { it.balance.amount }
        val walletsSorted = list.sortedWith(balanceComparator.thenBy(WalletBalance::walletAddress))
        WalletsModel(totalBalance, walletsSorted.size, walletsSorted)
      }

  fun getWalletsModel(): Single<WalletsModel> = retrieveWallets().filter { it.isNotEmpty() }
    .flatMapIterable { list -> list }
    .flatMap { getWalletInfoUseCase(it.address, cached = true, updateFiat = false).toObservable() }
    .map {
      WalletBalance(
        it.name,
        it.wallet,
        it.walletBalance.overallFiat,
        preferencesRepository.getCurrentWalletAddress() == it.wallet,
        it.backupDate
      )
    }
    .doOnError { logger.log("WalletsInteract", it) }
    .toList()
    .map { list ->
      val totalBalance = getTotalBalance(list)
      val balanceComparator = compareByDescending<WalletBalance> { it.balance.amount }
      val walletsSorted = list.sortedWith(balanceComparator.thenBy(WalletBalance::walletAddress))
      WalletsModel(totalBalance, walletsSorted.size, walletsSorted)
    }

  fun createWallet(name: String?): Completable = walletCreatorInteract.create(name)
    .subscribeOn(Schedulers.io())
    .flatMapCompletable { wallet ->
      getCurrentPromoCodeUseCase()
        .flatMap { walletCreatorInteract.setDefaultWallet(wallet.address).toSingleDefault(it) }
        .flatMap { gamificationRepository.getUserLevel(wallet.address, it.code) }
        .doOnSuccess { supportInteractor.registerUser(it, wallet.address) }
        .ignoreElement()
    }

  private fun getTotalBalance(walletBalance: List<WalletBalance>): FiatValue {
    if (walletBalance.isEmpty()) return FiatValue()
    val totalBalance = walletBalance.sumByBigDecimal { it.balance.amount }
    val wallet = walletBalance[0]
    return FiatValue(totalBalance, wallet.balance.currency, wallet.balance.symbol)
  }

  private fun retrieveWallets(): Observable<List<Wallet>> = fetchWalletsInteract.fetch()
    .subscribeOn(Schedulers.io())
    .map { it.toList() }
    .toObservable()
}
