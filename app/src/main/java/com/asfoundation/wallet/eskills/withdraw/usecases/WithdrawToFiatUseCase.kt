package com.asfoundation.wallet.eskills.withdraw.usecases

import com.asfoundation.wallet.eskills.withdraw.domain.FailedWithdraw
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawRepository
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.common.extensions.isEmailValid
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class WithdrawToFiatUseCase @Inject constructor(
  private val ewtObtainer: EwtAuthenticatorService,
  private val withdrawRepository: WithdrawRepository,
) {

  operator fun invoke(email: String, amount: BigDecimal): Single<WithdrawResult> {
    return ewtObtainer.getEwtAuthentication()
      .flatMap {
        return@flatMap if (email.isEmailValid()) {
          withdrawRepository.withdrawAppcCredits(it, email, amount)
        } else {
          Single.just(FailedWithdraw.InvalidEmailError)
        }
      }
  }
}
