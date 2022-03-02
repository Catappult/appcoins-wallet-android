package com.asfoundation.wallet.abtesting.db

import com.asfoundation.wallet.abtesting.Experiment
import com.asfoundation.wallet.abtesting.ExperimentModel
import io.reactivex.Completable
import io.reactivex.Single

interface ExperimentPersistence {

  fun save(experimentName: String, experiment: Experiment): Completable

  operator fun get(identifier: String): Single<ExperimentModel>
}
