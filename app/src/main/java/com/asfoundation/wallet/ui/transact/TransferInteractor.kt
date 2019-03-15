package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.GetDefaultWalletBalance
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.util.BalanceUtils
import io.reactivex.Single
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.UnknownHostException

class TransferInteractor(private val rewardsManager: RewardsManager,
                         private val transactionDataValidator: TransactionDataValidator,
                         private val balanceInteractor: GetDefaultWalletBalance,
                         private val findDefaultWalletInteract: FindDefaultWalletInteract) {

  fun transferCredits(toWallet: String, amount: BigDecimal,
                      packageName: String): Single<AppcoinsRewardsRepository.Status> {
    return rewardsManager.balance.map {
      transactionDataValidator.validateData(toWallet, amount.multiply(BigDecimal.TEN.pow(18)), it)
    }
        .flatMap {
          when (it) {
            TransactionDataValidator.DataStatus.OK -> rewardsManager.sendCredits(toWallet, amount,
                packageName)
            TransactionDataValidator.DataStatus.INVALID_AMOUNT -> Single.just(
                AppcoinsRewardsRepository.Status.INVALID_AMOUNT)
            TransactionDataValidator.DataStatus.INVALID_WALLET_ADDRESS -> Single.just(
                AppcoinsRewardsRepository.Status.INVALID_WALLET_ADDRESS)
            TransactionDataValidator.DataStatus.NOT_ENOUGH_FUNDS -> Single.just(
                AppcoinsRewardsRepository.Status.NOT_ENOUGH_FUNDS)
          }
        }.onErrorReturn { map(it) }
  }

  private fun map(throwable: Throwable): AppcoinsRewardsRepository.Status {
    return when (throwable) {
      is UnknownHostException -> return AppcoinsRewardsRepository.Status.NO_INTERNET
      else -> AppcoinsRewardsRepository.Status.UNKNOWN_ERROR
    }
  }

  fun getCreditsBalance(): Single<BigDecimal> {
    return rewardsManager.balance.map {
      BalanceUtils.weiToEth(it).setScale(4, RoundingMode.HALF_UP)
    }
  }

  fun getAppcoinsBalance(): Single<BigDecimal> {
    return findDefaultWalletInteract.find().flatMap { balanceInteractor.getTokens(it) }
        .map { BigDecimal(it.value) }
  }

  fun getEthBalance(): Single<BigDecimal> {
    return findDefaultWalletInteract.find().flatMap { balanceInteractor.getEthereumBalance(it) }
        .map { BigDecimal(it.value) }
  }
}
