package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.repository.ImpressionRepository
import io.reactivex.Completable
import javax.inject.Inject

class GetImpressionUseCase
@Inject
constructor(
  private val impressionRepository: ImpressionRepository,
  private val schedulers: RxSchedulers
) {
  operator fun invoke(): Completable {
    return impressionRepository.getImpression().subscribeOn(schedulers.io)
  }
}