package com.asfoundation.wallet.eskills.withdraw.repository

import com.asfoundation.wallet.eskills.withdraw.domain.SuccessfulWithdraw
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal


class WithdrawRepository(
    private val withdrawApi: WithdrawApi,
    private val mapper: WithdrawApiMapper,
    private val scheduler: Scheduler,
    private val withdrawLocalStorage: WithdrawLocalStorage
) {
  fun getAvailableAmount(ewt: String): Single<BigDecimal> {
    return withdrawApi.getAvailableAmount(ewt)
        .subscribeOn(scheduler)
        .map { it.amount }
  }

  fun withdrawAppcCredits(ewt: String, email: String, amount: BigDecimal): Single<WithdrawResult> {
    withdrawLocalStorage.saveUserEmail(email)
    return withdrawApi.withdrawAppcCredits(ewt, WithdrawBody(email, amount))
        .subscribeOn(scheduler)
        .andThen(Single.just(SuccessfulWithdraw(amount) as WithdrawResult))
        .onErrorReturn { mapper.map(it) }
  }

  fun getStoredUserEmail(): Single<String> {
    return withdrawLocalStorage.getUserEmail()
  }
}
