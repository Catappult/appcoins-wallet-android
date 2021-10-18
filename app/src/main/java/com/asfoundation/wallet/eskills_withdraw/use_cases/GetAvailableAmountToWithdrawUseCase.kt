package com.asfoundation.wallet.eskills_withdraw.use_cases

import com.asfoundation.wallet.eskills_withdraw.repository.WithdrawAvailableAmount
import com.asfoundation.wallet.eskills_withdraw.repository.WithdrawRepository
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import io.reactivex.Single

class GetAvailableAmountToWithdrawUseCase(
    private val ewtObtainer: EwtAuthenticatorService,
    private val withdrawRepository: WithdrawRepository,
) {

  operator fun invoke(): Single<WithdrawAvailableAmount> {
    return ewtObtainer.getEwtAuthentication()
        .flatMap {
          withdrawRepository.getAvailableAmount(it)
        }
  }
}
