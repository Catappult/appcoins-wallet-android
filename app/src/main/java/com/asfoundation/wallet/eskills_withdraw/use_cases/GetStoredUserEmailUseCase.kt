package com.asfoundation.wallet.eskills_withdraw.use_cases

import com.asfoundation.wallet.eskills_withdraw.repository.WithdrawRepository
import io.reactivex.Single

class GetStoredUserEmailUseCase(private val withdrawRepository: WithdrawRepository) {
  operator fun invoke(): Single<String> {
    return withdrawRepository.getStoredUserEmail()
  }
}
