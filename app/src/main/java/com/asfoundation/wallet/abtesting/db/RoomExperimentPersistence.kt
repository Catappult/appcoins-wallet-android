package com.asfoundation.wallet.abtesting.db

import com.asfoundation.wallet.abtesting.Experiment
import com.asfoundation.wallet.abtesting.ExperimentModel
import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class RoomExperimentPersistence @Inject constructor(private val experimentDao: ExperimentDao,
                                                    private val rxSchedulers: RxSchedulers) :
    ExperimentPersistence {

  private val mapper: RoomExperimentMapper = RoomExperimentMapper()

  override fun save(experimentName: String, experiment: Experiment): Completable {
    return Completable.create {
      experimentDao.save(mapper.map(experimentName, experiment))
      it.onComplete()
    }
        .subscribeOn(rxSchedulers.io)
  }

  override fun get(identifier: String): Single<ExperimentModel> {
    return experimentDao[identifier]
        .subscribeOn(rxSchedulers.io)
        .flatMap { roomExperiment ->
          Single.just(ExperimentModel(mapper.map(roomExperiment), false))
        }
        .onErrorReturn { ExperimentModel(Experiment(), true) }
  }
}

