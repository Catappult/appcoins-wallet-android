package com.asfoundation.wallet.eskills.withdraw.usecases

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawRepository
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class GetAvailableAmountToWithdrawUseCase @Inject constructor(
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
