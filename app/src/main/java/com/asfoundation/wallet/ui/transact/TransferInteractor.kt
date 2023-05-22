package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract
import com.appcoins.wallet.feature.walletInfo.data.usecases.GetWalletInfoUseCase
import io.reactivex.Single
import java.math.BigDecimal
import java.net.UnknownHostException
import javax.inject.Inject

class TransferInteractor @Inject constructor(private val rewardsManager: RewardsManager,
                                             private val transactionDataValidator: TransactionDataValidator,
                                             private val getWalletInfoUseCase: com.appcoins.wallet.feature.walletInfo.data.usecases.GetWalletInfoUseCase,
                                             private val findDefaultWalletInteract: com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract,
                                             private val walletBlockedInteract: WalletBlockedInteract) {

  fun transferCredits(toWallet: String, amount: BigDecimal,
                      packageName: String): Single<AppcoinsRewardsRepository.Status> {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
        .map { walletInfo ->
          val creditsAmount = walletInfo.walletBalance.creditsBalance.token.amount
          transactionDataValidator.validateData(toWallet, amount, creditsAmount)
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
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
        .map { walletInfo ->
          walletInfo.walletBalance.creditsBalance.token.amount
        }
  }

  fun validateEthTransferData(walletAddress: String,
                              amount: BigDecimal): Single<AppcoinsRewardsRepository.Status> {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
        .map { walletInfo ->
          val ethAmount = walletInfo.walletBalance.ethBalance.token.amount
          validateData(transactionDataValidator.validateData(walletAddress, amount, ethAmount))
        }
  }

  fun validateAppcTransferData(walletAddress: String,
                               amount: BigDecimal): Single<AppcoinsRewardsRepository.Status> {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
        .map { walletInfo ->
          val appcAmount = walletInfo.walletBalance.appcBalance.token.amount
          validateData(transactionDataValidator.validateData(walletAddress, amount, appcAmount))
        }
  }

  fun isWalletBlocked(): Single<Boolean> = walletBlockedInteract.isWalletBlocked()

  fun find(): Single<Wallet> = findDefaultWalletInteract.find()
}
