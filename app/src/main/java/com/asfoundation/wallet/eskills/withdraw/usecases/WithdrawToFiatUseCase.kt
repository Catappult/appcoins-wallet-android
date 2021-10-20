package com.asfoundation.wallet.eskills.withdraw.usecases

import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.util.isEmailValid
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawRepository
import io.reactivex.Single
import java.math.BigDecimal

class WithdrawToFiatUseCase(
    private val ewtObtainer: EwtAuthenticatorService,
    private val withdrawRepository: WithdrawRepository,
) {

  operator fun invoke(email: String, amount: BigDecimal): Single<WithdrawResult> {
    return ewtObtainer.getEwtAuthentication()
        .flatMap {
          return@flatMap if (email.isEmailValid()) {
            withdrawRepository.withdrawAppcCredits(it, email, amount)
          } else {
            Single.just(WithdrawResult(amount, WithdrawResult.Status.INVALID_EMAIL))
          }
        }

  }
}
