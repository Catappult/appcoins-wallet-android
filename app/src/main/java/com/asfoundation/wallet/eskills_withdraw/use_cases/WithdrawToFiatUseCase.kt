package com.asfoundation.wallet.eskills_withdraw.use_cases

import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.util.isEmailValid
import com.asfoundation.wallet.eskills_withdraw.WithdrawResult
import com.asfoundation.wallet.eskills_withdraw.repository.WithdrawRepository
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
