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
          val validateStatus = validateData(it)
          if (validateStatus == AppcoinsRewardsRepository.Status.SUCCESS) {
            return@flatMap rewardsManager.sendCredits(toWallet, amount, packageName)
          }
          return@flatMap Single.just(validateStatus)
        }.onErrorReturn { map(it) }
  }

  private fun validateData(
      data: TransactionDataValidator.DataStatus): AppcoinsRewardsRepository.Status {
    return when (data) {
      TransactionDataValidator.DataStatus.OK -> AppcoinsRewardsRepository.Status.SUCCESS
      TransactionDataValidator.DataStatus.INVALID_AMOUNT -> AppcoinsRewardsRepository.Status.INVALID_AMOUNT
      TransactionDataValidator.DataStatus.INVALID_WALLET_ADDRESS -> AppcoinsRewardsRepository.Status.INVALID_WALLET_ADDRESS
      TransactionDataValidator.DataStatus.NOT_ENOUGH_FUNDS -> AppcoinsRewardsRepository.Status.NOT_ENOUGH_FUNDS
    }
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
    return findDefaultWalletInteract.find().flatMap { balanceInteractor.getTokens(it, 4) }
        .map { BigDecimal(it.value) }
  }

  fun getEthBalance(): Single<BigDecimal> {
    return findDefaultWalletInteract.find().flatMap { balanceInteractor.getEthereumBalance(it) }
        .map { BigDecimal(it.value) }
  }

  fun validateEthTransferData(walletAddress: String,
                              amount: BigDecimal): Single<AppcoinsRewardsRepository.Status> {
    return getEthBalance().map {
      validateData(transactionDataValidator.validateData(walletAddress, amount, it))
    }
  }

  fun validateAppcTransferData(walletAddress: String,
                               amount: BigDecimal): Single<AppcoinsRewardsRepository.Status> {
    return getAppcoinsBalance().map {
      validateData(transactionDataValidator.validateData(walletAddress, amount, it))
    }
  }
}
