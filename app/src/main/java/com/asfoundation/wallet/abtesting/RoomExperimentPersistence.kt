package com.asfoundation.wallet.abtesting

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single

class RoomExperimentPersistence(private val experimentDao: ExperimentDao,
                                private val mapper: RoomExperimentMapper,
                                private val ioScheduler: Scheduler) : ExperimentPersistence {

  override fun save(experimentName: String, experiment: Experiment): Completable {
    return Completable.create {
      experimentDao.save(mapper.map(experimentName, experiment))
      it.onComplete()
    }
        .subscribeOn(ioScheduler)
  }

  override fun get(identifier: String): Single<ExperimentModel> {
    return experimentDao[identifier]
        .subscribeOn(ioScheduler)
        .flatMap { roomExperiment ->
          Single.just(ExperimentModel(mapper.map(roomExperiment), false))
        }
        .onErrorReturn { ExperimentModel(Experiment(), true) }
  }
}

