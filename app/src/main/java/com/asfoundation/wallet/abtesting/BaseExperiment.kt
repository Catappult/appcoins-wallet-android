package com.asfoundation.wallet.abtesting

import io.reactivex.Single

interface BaseExperiment {

  fun getConfiguration(): Single<String>
  fun mapConfiguration(config: String): Any
  val type: ExperimentType

  enum class ExperimentType {
    RAKAM, WASABI
  }
}
