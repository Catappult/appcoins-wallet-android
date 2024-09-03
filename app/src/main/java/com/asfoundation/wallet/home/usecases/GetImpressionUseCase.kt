package com.asfoundation.wallet.home.usecases


import com.asfoundation.wallet.repository.ImpressionRepository
import io.reactivex.Completable
import javax.inject.Inject

class GetImpressionUseCase
@Inject
constructor(
  private val impressionRepository: ImpressionRepository,
) {
  operator fun invoke(): Completable {
    return impressionRepository.getImpression()
  }
}