package com.asfoundation.wallet.abtesting.experiments

import com.asfoundation.wallet.abtesting.ABTestInteractor
import io.reactivex.Single

class BalanceWalletsExperiment(private val abTestInteractor: ABTestInteractor) :
    WasabiExperiment() {

  private companion object {
    private const val EXPERIMENT_ID = "WAL-78-Balance-vs-Wallets"
    private val experimentValues = listOf("Balance", "Wallets")
  }

  private var assignment: String? = null

  override fun getConfiguration(): Single<String> {
    if (assignment != null) {
      return Single.just(assignment)
    }
    return abTestInteractor.getExperiment(EXPERIMENT_ID, type)
        .flatMap { experiment ->
          var experimentAssignment: String? = "Balance"
          if (!experiment.experimentOver && experiment.partOfExperiment) {
            experimentAssignment = experiment.assignment
          }
          if (experimentValues.contains(experimentAssignment)) assignment = experimentAssignment
          Single.just(assignment)
        }
  }
}