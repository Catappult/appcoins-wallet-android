package com.asfoundation.wallet.abtesting.db

import com.asfoundation.wallet.abtesting.Experiment

class RoomExperimentMapper {

  fun map(experimentName: String, experiment: Experiment): ExperimentEntity {
    return ExperimentEntity(experimentName, experiment.requestTime, experiment.assignment,
        experiment.payload, experiment.partOfExperiment, experiment.experimentOver)
  }

  fun map(experiment: ExperimentEntity): Experiment {
    return Experiment(experiment.requestTime, experiment.assignment, experiment.payload,
        experiment.partOfExperiment, experiment.experimentOver)
  }
}
