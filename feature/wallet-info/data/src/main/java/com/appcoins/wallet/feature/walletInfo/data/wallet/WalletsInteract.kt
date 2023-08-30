package com.appcoins.wallet.feature.walletInfo.data.wallet

import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.core.utils.android_common.extensions.sumByBigDecimal
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.ObserveWalletInfoUseCase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import com.wallet.appcoins.feature.support.data.SupportInteractor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WalletsInteract @Inject constructor(
  private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val fetchWalletsInteract: FetchWalletsInteract,
  private val walletCreatorInteract: WalletCreatorInteract,
  private val supportInteractor: SupportInteractor,
  private val preferencesRepository: CommonsPreferencesDataSource,
  private val gamificationRepository: Gamification,
  private val logger: Logger,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {

  fun observeWalletsModel(): Observable<WalletsModel> =
    retrieveWallets().filter { it.isNotEmpty() }
      .flatMapIterable { it }
      .flatMap { observeWalletInfoUseCase(it.address, update = true) }
      .map {
        listOf(
          WalletInfoSimple(
            it.name,
            it.wallet,
            it.walletBalance.creditsOnlyFiat,
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
        val balanceComparator = compareByDescending<WalletInfoSimple> { it.balance.amount }
        val walletsSorted =
          list.sortedWith(balanceComparator.thenBy(WalletInfoSimple::walletAddress))
        WalletsModel(totalBalance, walletsSorted.size, walletsSorted)
      }

  fun getWalletsModel(): Single<WalletsModel> = retrieveWallets().filter { it.isNotEmpty() }
    .flatMapIterable { list -> list }
    .flatMap { getWalletInfoUseCase(it.address, cached = true).toObservable() }
    .map {
      WalletInfoSimple(
        it.name,
        it.wallet,
        it.walletBalance.creditsOnlyFiat,
        preferencesRepository.getCurrentWalletAddress() == it.wallet,
        it.backupDate
      )
    }
    .doOnError { logger.log("WalletsInteract", it) }
    .toList()
    .map { list ->
      val totalBalance = getTotalBalance(list)
      val balanceComparator = compareByDescending<WalletInfoSimple> { it.balance.amount }
      val walletsSorted = list.sortedWith(balanceComparator.thenBy(WalletInfoSimple::walletAddress))
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

  private fun getTotalBalance(walletBalance: List<WalletInfoSimple>): FiatValue {
    if (walletBalance.isEmpty()) return FiatValue()
    val totalBalance = walletBalance.sumByBigDecimal { it.balance.amount }
    val wallet = walletBalance[0]
    return FiatValue(
      totalBalance,
      wallet.balance.currency,
      wallet.balance.symbol
    )
  }

  private fun retrieveWallets(): Observable<List<Wallet>> = fetchWalletsInteract.fetch()
    .subscribeOn(Schedulers.io())
    .map { it.toList() }
    .toObservable()
}
