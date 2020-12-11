package com.asfoundation.wallet.abtesting.experiments.balancewallets

import com.asf.wallet.R
import com.asfoundation.wallet.abtesting.ABTestInteractor
import com.asfoundation.wallet.abtesting.experiments.RakamExperiment
import io.reactivex.Single

class BalanceWalletsExperiment(private val abTestInteractor: ABTestInteractor) :
    RakamExperiment() {

  companion object {
    internal const val NO_EXPERIMENT = "Default"
    private const val EXPERIMENT_ID = "WAL-78-Balance-vs-MyWallets"
    private val experimentValues = listOf("Balance", "MyWallets")
  }

  private var assignment: String? = null

  override fun getConfiguration(): Single<String> {
    if (assignment != null) {
      return Single.just(assignment)
    }
    return abTestInteractor.getExperiment(EXPERIMENT_ID, type)
        .flatMap { experiment ->
          var experimentAssignment = NO_EXPERIMENT
          if (!experiment.experimentOver && experiment.partOfExperiment && experiment.assignment != null) {
            experimentAssignment = experiment.assignment
          }
          if (experimentValues.contains(experimentAssignment)) assignment = experimentAssignment
          Single.just(experimentAssignment)
        }
  }

  override fun mapConfiguration(assignment: String): Int {
    return when (assignment) {
      "Balance" -> R.string.balance_title
      "MyWallets" -> R.string.wallets_title //TODO change for My Wallets string
      else -> R.string.balance_title
    }
  }
}