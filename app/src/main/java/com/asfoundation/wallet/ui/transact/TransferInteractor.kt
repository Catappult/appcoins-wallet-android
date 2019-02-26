package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.asfoundation.wallet.ui.iab.RewardsManager
import io.reactivex.Single
import java.math.BigDecimal

class TransferInteractor(private val rewardsManager: RewardsManager) {

  fun transferCredits(toWallet: String, amount: BigDecimal,
                      packageName: String): Single<AppcoinsRewardsRepository.Status> {
    return rewardsManager.sendCredits(toWallet, amount, packageName)
  }
}
