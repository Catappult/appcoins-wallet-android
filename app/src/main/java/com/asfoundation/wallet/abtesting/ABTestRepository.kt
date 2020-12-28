package com.asfoundation.wallet.abtesting

import io.reactivex.Observable

interface ABTestRepository {

  fun getExperiment(identifier: String): Observable<Experiment>

  //This method may be used, but it is encouraged to use Rakam log instead
  fun recordImpression(identifier: String,
                       type: BaseExperiment.ExperimentType): Observable<Boolean>

  //This method may be used, but it is encouraged to use Rakam log instead
  fun recordAction(identifier: String,
                   type: BaseExperiment.ExperimentType): Observable<Boolean>
}
