package com.asfoundation.wallet.withdraw.repository

import io.reactivex.Completable
import java.math.BigDecimal


class WithdrawRepository(private val withdrawApi: WithdrawApi) {

  fun withdrawAppcCredits(ewt: String, email: String, amount: BigDecimal): Completable {
    return withdrawApi.withdrawAppcCredits(ewt, WithdrawBody(email, amount))
  }

}