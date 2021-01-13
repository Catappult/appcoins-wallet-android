package com.asfoundation.wallet.abtesting

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
