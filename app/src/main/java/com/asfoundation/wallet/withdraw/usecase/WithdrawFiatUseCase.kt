package com.asfoundation.wallet.withdraw.usecase

import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.withdraw.repository.WithdrawRepository
import io.reactivex.Completable
import java.math.BigDecimal

class WithdrawFiatUseCase(
  private val ewtObtainer: EwtAuthenticatorService,
  private val repository: WithdrawRepository,
) {

  fun execute(email: String, amount: BigDecimal): Completable {
    return ewtObtainer.getEwtAuthentication()
      .flatMapCompletable { repository.withdrawAppcCredits(it, email, amount) }
  }
}