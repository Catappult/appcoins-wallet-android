package com.asfoundation.wallet.abtesting.experiments.topup

import com.asfoundation.wallet.abtesting.ABTestInteractor
import com.asfoundation.wallet.abtesting.experiments.RakamExperiment
import io.reactivex.Single
import javax.inject.Inject

class TopUpDefaultValueExperiment @Inject constructor(
    private val abTestInteractor: ABTestInteractor) :
    RakamExperiment() {

  companion object {
    internal const val NO_EXPERIMENT = "Default"
    private const val EXPERIMENT_ID = "APPC-2448-topup-default-value"
    private val experimentValues = listOf("Index1", "Index2")
  }

  private var assignment: String? = null

  override fun getConfiguration(): Single<String> {
    if (assignment != null) {
      return Single.just(assignment)
    }
    return abTestInteractor.getExperiment(EXPERIMENT_ID)
        .flatMap { experiment ->
          var experimentAssignment = NO_EXPERIMENT
          if (!experiment.experimentOver && experiment.partOfExperiment && experiment.assignment != null) {
            experimentAssignment = experiment.assignment
          }
          if (experimentValues.contains(experimentAssignment)) assignment = experimentAssignment
          Single.just(experimentAssignment)
        }
  }

  override fun mapConfiguration(assignment: String?): Int {
    return when (assignment) {
      "Index1" -> 1
      "Index2" -> 2
      else -> 1
    }
  }

  override fun getCachedAssignment(): Int {
    return mapConfiguration(assignment)
  }
}