package com.asfoundation.wallet.abtesting

import io.reactivex.Completable
import io.reactivex.Observable

interface ABTestRepository {

  fun getExperiment(identifier: String,
                    type: BaseExperiment.ExperimentType?): Observable<Experiment>

  fun recordImpression(identifier: String,
                       type: BaseExperiment.ExperimentType): Observable<Boolean>

  fun recordAction(identifier: String,
                   type: BaseExperiment.ExperimentType): Observable<Boolean>

  fun recordAction(identifier: String, position: Int,
                   type: BaseExperiment.ExperimentType): Observable<Boolean>

  fun cacheExperiment(experiment: ExperimentModel,
                      experimentName: String): Completable

  fun getExperimentId(id: String): Observable<String>
}
