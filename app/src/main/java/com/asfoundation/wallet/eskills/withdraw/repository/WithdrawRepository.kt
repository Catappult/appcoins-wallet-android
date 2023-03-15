package com.asfoundation.wallet.eskills.withdraw.repository

import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.eskills.withdraw.domain.SuccessfulWithdraw
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class WithdrawRepository @Inject constructor(
  private val withdrawApi: WithdrawApi,
  private val mapper: WithdrawApiMapper,
  private val schedulers: com.appcoins.wallet.ui.arch.RxSchedulers,
  private val withdrawLocalStorage: WithdrawLocalStorage
) {

  fun getAvailableAmount(ewt: String): Single<BigDecimal> {
    return withdrawApi.getAvailableAmount(ewt)
      .subscribeOn(schedulers.io)
      .map { it.amount }
  }

  fun withdrawAppcCredits(ewt: String, email: String, amount: BigDecimal): Single<WithdrawResult> {
    withdrawLocalStorage.saveUserEmail(email)
    return withdrawApi.withdrawAppcCredits(ewt, WithdrawBody(email, amount))
      .subscribeOn(schedulers.io)
      .andThen(Single.just(SuccessfulWithdraw(amount) as WithdrawResult))
      .onErrorReturn { mapper.map(it) }
  }

  fun getStoredUserEmail(): Single<String> {
    return withdrawLocalStorage.getUserEmail()
  }
}
