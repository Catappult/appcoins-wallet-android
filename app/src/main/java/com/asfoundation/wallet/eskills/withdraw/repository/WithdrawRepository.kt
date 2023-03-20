package com.asfoundation.wallet.eskills.withdraw.repository

import com.appcoins.wallet.core.network.backend.api.WithdrawApi
import com.appcoins.wallet.core.network.backend.model.WithdrawBody
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.eskills.withdraw.domain.SuccessfulWithdraw
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import io.reactivex.Single
import com.appcoins.wallet.sharedpreferences.WithdrawPreferencesDataSource
import java.math.BigDecimal
import javax.inject.Inject

class WithdrawRepository @Inject constructor(
  private val withdrawApi: WithdrawApi,
  private val mapper: WithdrawApiMapper,
  private val schedulers: RxSchedulers,
  private val withdrawLocalStorage: WithdrawPreferencesDataSource
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
    return Single.fromCallable {
      return@fromCallable withdrawLocalStorage.getUserEmail() ?: throw RuntimeException(
        "Couldn't find user e-mail."
      )
    }
  }
}
