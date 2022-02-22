package com.asfoundation.wallet.abtesting

import javax.inject.Inject
import javax.inject.Named

class ABTestCacheValidator @Inject constructor(
    @Named("ab-test-local-cache") private val localCache: HashMap<String, ExperimentModel>) {

  fun isCacheValid(experimentId: String): Boolean {
    return localCache.containsKey(experimentId) && !localCache[experimentId]!!.hasError &&
        !localCache[experimentId]!!.experiment.experimentOver &&
        localCache[experimentId]!!.experiment.partOfExperiment
  }

  fun isExperimentValid(experimentId: String): Boolean {
    val model = localCache[experimentId]
    return if (model == null) false
    else {
      !model.experiment.isExpired() && !model.hasError && !model.experiment.experimentOver &&
          model.experiment.partOfExperiment
    }
  }
}
