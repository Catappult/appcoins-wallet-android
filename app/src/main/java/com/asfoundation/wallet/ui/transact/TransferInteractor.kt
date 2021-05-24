package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.util.BalanceUtils
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import com.asfoundation.wallet.wallets.GetDefaultWalletBalanceInteract
import io.reactivex.Single
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.UnknownHostException

class TransferInteractor(private val rewardsManager: RewardsManager,
                         private val transactionDataValidator: TransactionDataValidator,
                         private val balanceInteractor: GetDefaultWalletBalanceInteract,
                         private val findDefaultWalletInteract: FindDefaultWalletInteract,
                         private val walletBlockedInteract: WalletBlockedInteract) {

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
        }
        .onErrorReturn { map(it) }
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
      BalanceUtils.weiToEth(it)
          .setScale(4, RoundingMode.FLOOR)
    }
  }

  fun getAppcoinsBalance(): Single<BigDecimal> {
    return findDefaultWalletInteract.find()
        .flatMap { balanceInteractor.getAppcBalance(it.address) }
        .map { it.value }
  }

  fun getEthBalance(): Single<BigDecimal> {
    return findDefaultWalletInteract.find()
        .flatMap { balanceInteractor.getEthereumBalance(it.address) }
        .map { it.value }
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

  fun isWalletBlocked(): Single<Boolean> = walletBlockedInteract.isWalletBlocked()

  fun find(): Single<Wallet> = findDefaultWalletInteract.find()
}
