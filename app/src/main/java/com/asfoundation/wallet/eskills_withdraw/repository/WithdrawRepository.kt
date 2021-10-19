package com.asfoundation.wallet.eskills_withdraw.repository

import com.asfoundation.wallet.eskills_withdraw.WithdrawResult
import io.reactivex.Scheduler
import io.reactivex.Single
import java.math.BigDecimal


class WithdrawRepository(
    private val withdrawApi: WithdrawApi,
    private val mapper: WithdrawApiMapper,
    private val scheduler: Scheduler
) {
  fun getAvailableAmount(ewt: String): Single<WithdrawAvailableAmount> {
    return withdrawApi.getAvailableAmount(ewt).subscribeOn(scheduler)
  }

  fun withdrawAppcCredits(ewt: String, email: String, amount: BigDecimal): Single<WithdrawResult> {
    return withdrawApi.withdrawAppcCredits(ewt, WithdrawBody(email, amount))
        .subscribeOn(scheduler)
        .andThen(Single.just(WithdrawResult(amount, WithdrawResult.Status.SUCCESS)))
        .onErrorReturn { mapper.map(amount, it) }
  }
}
