package com.asfoundation.wallet.abtesting

import io.reactivex.Observable
import io.reactivex.Single


class ABTestInteractor(private val abTestRepository: ABTestRepository) {

  fun getExperiment(identifier: String): Single<Experiment> {
    return abTestRepository.getExperiment(identifier)
        .firstOrError()
  }

  //This method may be used, but it is encouraged to use Rakam log instead
  fun recordImpression(identifier: String,
                       type: BaseExperiment.ExperimentType): Observable<Boolean> {
    return abTestRepository.recordImpression(identifier, type)
  }

  //This method may be used, but it is encouraged to use Rakam log instead
  fun recordAction(identifier: String,
                   type: BaseExperiment.ExperimentType): Observable<Boolean> {
    return abTestRepository.recordAction(identifier, type)
  }
}