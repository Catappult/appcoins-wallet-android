package com.asfoundation.wallet.eskills.withdraw.usecases

import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawRepository
import io.reactivex.Single
import javax.inject.Inject

class GetStoredUserEmailUseCase @Inject constructor(
    private val withdrawRepository: WithdrawRepository) {
  operator fun invoke(): Single<String> {
    return withdrawRepository.getStoredUserEmail()
  }
}
