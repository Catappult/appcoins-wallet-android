package com.asfoundation.wallet.withdraw.usecase

import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.util.isEmailValid
import com.asfoundation.wallet.withdraw.WithdrawResult
import com.asfoundation.wallet.withdraw.repository.WithdrawRepository
import io.reactivex.Single
import java.math.BigDecimal

class WithdrawFiatUseCase(
  private val ewtObtainer: EwtAuthenticatorService,
  private val repository: WithdrawRepository,
) {

  fun execute(email: String, amount: BigDecimal): Single<WithdrawResult> {
    return ewtObtainer.getEwtAuthentication()
      .flatMap {
        return@flatMap if (email.isEmailValid()) {
          repository.withdrawAppcCredits(it, email, amount)
        } else {
          Single.just(WithdrawResult(WithdrawResult.Status.INVALID_EMAIL))
        }
      }

  }
}