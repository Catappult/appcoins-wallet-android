package com.asfoundation.wallet.abtesting

import io.reactivex.Completable
import io.reactivex.Single

interface ExperimentPersistence {

  fun save(experimentName: String, experiment: Experiment): Completable

  operator fun get(identifier: String): Single<ExperimentModel>
}
