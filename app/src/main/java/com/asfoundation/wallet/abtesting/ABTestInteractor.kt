package com.asfoundation.wallet.abtesting

import io.reactivex.Observable
import io.reactivex.Single


class ABTestInteractor(private val abTestRepository: ABTestRepository) {

  fun getExperiment(identifier: String,
                    type: BaseExperiment.ExperimentType): Single<Experiment> {
    return abTestRepository.getExperiment(identifier, type)
        .firstOrError()
  }

  fun recordImpression(identifier: String,
                       type: BaseExperiment.ExperimentType): Observable<Boolean> {
    return abTestRepository.recordImpression(identifier, type)
  }

  fun recordAction(identifier: String,
                   type: BaseExperiment.ExperimentType): Observable<Boolean> {
    return abTestRepository.recordAction(identifier, type)
  }

  fun recordAction(identifier: String, position: Int,
                   type: BaseExperiment.ExperimentType): Observable<Boolean> {
    return abTestRepository.recordAction(identifier, position, type)
  }

  fun getExperimentId(id: String): Observable<String> {
    return abTestRepository.getExperimentId(id)
  }
}