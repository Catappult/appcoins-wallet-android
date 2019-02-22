package com.asfoundation.wallet.ui.transact

import com.asfoundation.wallet.ui.iab.RewardsManager
import io.reactivex.Completable
import java.math.BigDecimal

class TransferInteractor(private val rewardsManager: RewardsManager) {

  fun transferCredits(toWallet: String, amount: BigDecimal, packageName: String): Completable {
    return rewardsManager.sendCredits(toWallet, amount, packageName)
  }
}
