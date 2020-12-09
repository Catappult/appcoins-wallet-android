package com.asfoundation.wallet.abtesting

import io.reactivex.Single

interface BaseExperiment {

  fun getConfiguration(): Single<String>
  val type: ExperimentType

  enum class ExperimentType {
    RAKAM, WASABI
  }
}
