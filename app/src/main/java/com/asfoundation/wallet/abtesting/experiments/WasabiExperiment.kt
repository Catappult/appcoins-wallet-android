package com.asfoundation.wallet.abtesting.experiments

import com.asfoundation.wallet.abtesting.BaseExperiment

abstract class WasabiExperiment : BaseExperiment {

  override val type: BaseExperiment.ExperimentType
    get() = BaseExperiment.ExperimentType.WASABI
}
