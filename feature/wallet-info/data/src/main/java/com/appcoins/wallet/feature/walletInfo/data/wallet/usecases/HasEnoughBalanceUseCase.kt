package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import io.reactivex.Single
import org.web3j.utils.Convert
import java.math.BigDecimal
import javax.inject.Inject

class HasEnoughBalanceUseCase @Inject constructor(
    private val getWalletInfoUseCase: GetWalletInfoUseCase) {

  enum class BalanceType {
    APPC, ETH, APPC_C
  }

  data class Value(val amount: BigDecimal, val unit: Convert.Unit, val balanceType: BalanceType)

  operator fun invoke(address: String?, value: BigDecimal, unit: Convert.Unit,
                      balanceType: BalanceType): Single<Boolean> {
    return getWalletInfoUseCase(address, cached = true)
        .flatMap { walletInfo ->
          val scaledValue = Convert.toWei(value, unit)
          val scaledCredits = Convert.toWei(walletInfo.walletBalance.creditsBalance.token.amount,
              Convert.Unit.ETHER)
          val scaledAppc =
              Convert.toWei(walletInfo.walletBalance.appcBalance.token.amount, Convert.Unit.ETHER)
          val scaledEth =
              Convert.toWei(walletInfo.walletBalance.ethBalance.token.amount, Convert.Unit.ETHER)
          return@flatMap when (balanceType) {
            BalanceType.APPC_C -> Single.just(scaledCredits >= scaledValue)
            BalanceType.APPC -> Single.just(scaledAppc >= scaledValue)
            BalanceType.ETH -> Single.just(scaledEth >= scaledValue)
          }
        }
  }
}