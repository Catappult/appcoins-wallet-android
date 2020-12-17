package com.asfoundation.wallet.abtesting

import com.asfoundation.wallet.abtesting.ABTestStatus.*
import com.asfoundation.wallet.identification.IdsRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler

class ABTestCenterRepository(private val apiProvider: ABTestApiProvider,
                             private val idsRepository: IdsRepository,
                             private val localCache: HashMap<String, ExperimentModel>,
                             private val persistence: RoomExperimentPersistence,
                             private val cacheValidator: ABTestCacheValidator,
                             private val ioScheduler: Scheduler) : ABTestRepository {

  override fun getExperiment(identifier: String,
                             type: BaseExperiment.ExperimentType?): Observable<Experiment> {
    return if (localCache.containsKey(identifier)) {
      if (cacheValidator.isExperimentValid(identifier)) {
        Observable.just(localCache[identifier]!!.experiment)
      } else {
        retrieveFromApiAndCacheExperiment(identifier, type)
      }
    } else {
      retrieveExperimentFromDb(identifier, type)
    }
  }

  override fun recordImpression(identifier: String,
                                type: BaseExperiment.ExperimentType): Observable<Boolean> {
    return if (cacheValidator.isCacheValid(identifier)) {
      return getAndroidId()
          .flatMap { id ->
            apiProvider.getApi(type)
                .recordImpression(identifier, id, ABTestRequestBody(IMPRESSION.name))
                .toObservable<Boolean>()
                .map { true }
                .doOnError { it.printStackTrace() }
                .onErrorReturn { false }
          }
    } else {
      Observable.just(false)
    }
  }

  override fun recordAction(identifier: String,
                            type: BaseExperiment.ExperimentType): Observable<Boolean> {
    return if (cacheValidator.isCacheValid(identifier)) {
      getExperiment(identifier, null)
          .flatMap {
            getAndroidId().flatMap { id ->
              apiProvider.getApi(type)
                  .recordAction(identifier, id, ABTestRequestBody(it.assignment))
                  .toObservable<Boolean>()
                  .map { true }
                  .doOnError { it.printStackTrace() }
                  .onErrorReturn { false }
            }
          }
    } else {
      Observable.just(false)
    }
  }

  private fun cacheExperiment(experiment: ExperimentModel, experimentName: String): Completable {
    return Completable.fromAction { localCache[experimentName] = experiment }
        .andThen(persistence.save(experimentName, experiment.experiment))
  }

  private fun getExperimentFromApi(identifier: String,
                                   type: BaseExperiment.ExperimentType): Observable<ExperimentModel> {
    return getAndroidId()
        .flatMap {
          apiProvider.getApi(type)
              .getExperiment(identifier, it)
              .subscribeOn(ioScheduler)
        }
        .map { mapToExperimentModel(it) }
        .onErrorReturn { ExperimentModel(Experiment(), true) }
  }

  private fun mapToExperimentModel(response: ABTestImpressionResponse): ExperimentModel {
    return ExperimentModel(Experiment(System.currentTimeMillis(),
        response.assignment, response.payload, mapExperimentStatus(response)))
  }

  private fun mapExperimentStatus(response: ABTestImpressionResponse): Boolean {
    val status = response.status
    return status == EXPERIMENT_OVER || status == EXPERIMENT_PAUSED || status == EXPERIMENT_NOT_FOUND
        || status == EXPERIMENT_DRAFT
  }

  private fun getAndroidId(): Observable<String> {
    return Observable.just(idsRepository.getAndroidId())
  }

  private fun retrieveExperimentFromDb(identifier: String,
                                       type: BaseExperiment.ExperimentType?): Observable<Experiment> {
    return persistence[identifier]
        .toObservable()
        .observeOn(ioScheduler)
        .flatMap { model ->
          if (!model.hasError && !model.experiment.isExpired()) {
            if (!localCache.containsKey(identifier)) {
              localCache[identifier] = model
            }
            Observable.just(model.experiment)
          } else {
            retrieveFromApiAndCacheExperiment(identifier, type)
          }
        }
  }

  private fun retrieveFromApiAndCacheExperiment(identifier: String,
                                                type: BaseExperiment.ExperimentType?): Observable<Experiment> {
    return getExperimentFromApi(identifier, type!!)
        .flatMap { experimentToCache ->
          cacheExperiment(experimentToCache, identifier).andThen(
              Observable.just(experimentToCache.experiment))
        }
  }
}