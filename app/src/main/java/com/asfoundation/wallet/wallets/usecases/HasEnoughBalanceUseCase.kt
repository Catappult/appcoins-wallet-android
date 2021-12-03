package com.asfoundation.wallet.wallets.usecases

import io.reactivex.Single
import org.web3j.utils.Convert
import java.math.BigDecimal

class HasEnoughBalanceUseCase(
    private val getWalletInfoUseCase: GetWalletInfoUseCase
) {

  enum class BalanceType {
    APPC, ETH, APPC_C
  }

  data class Value(val amount: BigDecimal, val unit: Convert.Unit, val balanceType: BalanceType)

  operator fun invoke(address: String?, value: BigDecimal, unit: Convert.Unit,
                      balanceType: BalanceType): Single<Boolean> {
    return getWalletInfoUseCase(address, cached = false, updateFiat = false)
        .flatMap { walletInfo ->
          val scaledValue = Convert.toWei(value, unit)
          return@flatMap when (balanceType) {
            BalanceType.APPC_C -> Single.just(
                walletInfo.walletBalance.creditsBalance.token.amount >= scaledValue)
            BalanceType.APPC -> Single.just(
                walletInfo.walletBalance.appcBalance.token.amount >= scaledValue)
            BalanceType.ETH -> Single.just(
                walletInfo.walletBalance.ethBalance.token.amount >= scaledValue)
          }
        }
  }
}