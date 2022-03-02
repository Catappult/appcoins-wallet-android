package com.asfoundation.wallet.abtesting

import com.asfoundation.wallet.abtesting.ABTestStatus.*
import com.asfoundation.wallet.abtesting.db.RoomExperimentPersistence
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.identification.IdsRepository
import io.reactivex.Completable
import io.reactivex.Observable
import it.czerwinski.android.hilt.annotations.BoundTo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Named

@BoundTo(supertype = ABTestRepository::class)
class ABTestCenterRepository @Inject constructor(private val api: ABTestApi,
                                                 private val idsRepository: IdsRepository,
                                                 @Named("ab-test-local-cache")
                                                 private val localCache: HashMap<String, ExperimentModel>,
                                                 private val persistence: RoomExperimentPersistence,
                                                 private val cacheValidator: ABTestCacheValidator,
                                                 private val rxSchedulers: RxSchedulers) :
    ABTestRepository {

  override fun getExperiment(identifier: String): Observable<Experiment> {
    return if (localCache.containsKey(identifier)) {
      if (cacheValidator.isExperimentValid(identifier)) {
        Observable.just(localCache[identifier]!!.experiment)
      } else {
        retrieveFromApiAndCacheExperiment(identifier)
      }
    } else {
      retrieveExperimentFromDb(identifier)
    }
  }

  override fun recordImpression(identifier: String,
                                type: BaseExperiment.ExperimentType): Observable<Boolean> {
    return if (cacheValidator.isCacheValid(identifier)) {
      return getAndroidId()
          .flatMap { id ->
            api.recordImpression(identifier, id, ABTestRequestBody(IMPRESSION.name))
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
      getExperiment(identifier)
          .flatMap {
            getAndroidId().flatMap { id ->
              api.recordAction(identifier, id, ABTestRequestBody(it.assignment))
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

  private fun getExperimentFromApi(identifier: String): Observable<ExperimentModel> {
    return getAndroidId()
        .flatMap {
          api.getExperiment(identifier, it)
              .subscribeOn(rxSchedulers.io)
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

  private fun retrieveExperimentFromDb(identifier: String): Observable<Experiment> {
    return persistence[identifier]
        .toObservable()
        .observeOn(rxSchedulers.io)
        .flatMap { model ->
          if (!model.hasError && !model.experiment.isExpired()) {
            if (!localCache.containsKey(identifier)) {
              localCache[identifier] = model
            }
            Observable.just(model.experiment)
          } else {
            retrieveFromApiAndCacheExperiment(identifier)
          }
        }
  }

  private fun retrieveFromApiAndCacheExperiment(identifier: String): Observable<Experiment> {
    return getExperimentFromApi(identifier)
        .flatMap { experimentToCache ->
          cacheExperiment(experimentToCache, identifier).andThen(
              Observable.just(experimentToCache.experiment))
        }
  }
}

interface ABTestApi {
  @GET("assignments/applications/Android/experiments/{experimentName}/users/{aptoideId}")
  fun getExperiment(@Path(value = "experimentName") experimentName: String,
                    @Path(value = "aptoideId")
                    aptoideId: String): Observable<ABTestImpressionResponse>

  @POST("events/applications/Android/experiments/{experimentName}/users/{aptoideId}")
  fun recordImpression(@Path(value = "experimentName") experimentName: String,
                       @Path(value = "aptoideId") aptoideId: String,
                       @Body body: ABTestRequestBody): Completable

  @POST("events/applications/Android/experiments/{experimentName}/users/{aptoideId}")
  fun recordAction(@Path(value = "experimentName") experimentName: String,
                   @Path(value = "aptoideId") aptoideId: String,
                   @Body body: ABTestRequestBody): Completable
}