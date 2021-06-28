package com.asfoundation.wallet.withdraw.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import com.asfoundation.wallet.withdraw.repository.WithdrawRepository
import io.reactivex.Completable
import java.math.BigDecimal

class WithdrawFiatUseCase(
  private val ewtObtainer: EwtObtainer,
  private val repository: WithdrawRepository,
) {

  fun execute(email: String, amount: BigDecimal): Completable {
    return ewtObtainer.getEWT()
      .flatMapCompletable { repository.withdrawAppcCredits(it, email, amount) }
  }
}