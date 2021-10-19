package com.asfoundation.wallet.eskills_withdraw.use_cases

import com.asfoundation.wallet.eskills_withdraw.repository.WithdrawRepository
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import io.reactivex.Single
import java.math.BigDecimal

class GetAvailableAmountToWithdrawUseCase(
    private val ewtObtainer: EwtAuthenticatorService,
    private val withdrawRepository: WithdrawRepository,
) {

  operator fun invoke(): Single<BigDecimal> {
    return ewtObtainer.getEwtAuthentication()
        .flatMap {
          withdrawRepository.getAvailableAmount(it)
        }
  }
}
