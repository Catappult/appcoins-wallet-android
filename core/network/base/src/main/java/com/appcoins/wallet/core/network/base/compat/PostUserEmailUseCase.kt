package com.appcoins.wallet.core.network.base.compat

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.sharedpreferences.HomePreferencesDataSource
import io.reactivex.Completable
import javax.inject.Inject

class PostUserEmailUseCase
@Inject
constructor(
  private val emailRepository: EmailRepository,
  private val homePreferencesDataSource: HomePreferencesDataSource,
  private val schedulers: RxSchedulers
) {
  operator fun invoke(email: String): Completable {
    return emailRepository.postUserEmail(email).doOnComplete {
      homePreferencesDataSource.saveWalletEmail(email)
    }.observeOn(schedulers.io)
  }
}