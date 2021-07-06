package com.asfoundation.wallet.withdraw.repository

import com.asfoundation.wallet.withdraw.WithdrawResult
import io.reactivex.Single
import java.math.BigDecimal


class WithdrawRepository(private val withdrawApi: WithdrawApi,
                         private val mapper: WithdrawApiMapper) {

  fun withdrawAppcCredits(ewt: String, email: String, amount: BigDecimal): Single<WithdrawResult> {
    return withdrawApi.withdrawAppcCredits(ewt, WithdrawBody(email, amount))
        .andThen(
            Single.just(
                WithdrawResult(WithdrawResult.Status.SUCCESS)
            )
        )
        .onErrorReturn { mapper.map(it) }
  }

}