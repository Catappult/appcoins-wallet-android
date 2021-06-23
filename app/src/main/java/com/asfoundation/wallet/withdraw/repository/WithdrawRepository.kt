package com.asfoundation.wallet.withdraw.repository

import io.reactivex.Completable
import java.math.BigDecimal


class WithdrawRepository(private val withdrawApi: WithdrawApi) {

  fun withdrawAppcCredits(email: String, amount: BigDecimal): Completable {
    return withdrawApi.withdrawAppcCredits(WithdrawBody(email, amount))
  }

}