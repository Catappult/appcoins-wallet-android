package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.asfoundation.wallet.ui.iab.RewardsManager
import io.reactivex.Single
import java.math.BigDecimal
import java.net.UnknownHostException

class TransferInteractor(private val rewardsManager: RewardsManager,
                         private val transactionDataValidator: TransactionDataValidator) {

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
}
