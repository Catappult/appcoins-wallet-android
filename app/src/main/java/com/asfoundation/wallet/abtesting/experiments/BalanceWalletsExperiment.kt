package com.asfoundation.wallet.abtesting.experiments

import com.asf.wallet.R
import com.asfoundation.wallet.abtesting.ABTestInteractor
import io.reactivex.Single

class BalanceWalletsExperiment(private val abTestInteractor: ABTestInteractor) :
    RakamExperiment() {

  private companion object {
    private const val EXPERIMENT_ID = "WAL-78-Balance-vs-MyWallets-test"
    private val experimentValues = listOf("Balance", "MyWallets")
  }

  private var assignment: String? = null

  override fun getConfiguration(): Single<String> {
    if (assignment != null) {
      return Single.just(assignment)
    }
    return abTestInteractor.getExperiment(EXPERIMENT_ID, type)
        .flatMap { experiment ->
          var experimentAssignment = "Balance"
          if (!experiment.experimentOver && experiment.partOfExperiment && experiment.assignment != null) {
            experimentAssignment = experiment.assignment
          }
          if (experimentValues.contains(experimentAssignment)) assignment = experimentAssignment
          Single.just(experimentAssignment)
        }
  }

  override fun mapConfiguration(config: String): Int {
    return when (config) {
      "Balance" -> R.string.balance_title
      "MyWallets" -> R.string.wallets_title //TODO change for My Wallets string
      else -> R.string.balance_title
    }
  }
}