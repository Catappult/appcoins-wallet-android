package com.asfoundation.wallet.abtesting

data class ExperimentModel(val experiment: Experiment, val hasError: Boolean = false)

data class Experiment(val requestTime: Long, val assignment: String?, val payload: String?,
                      val partOfExperiment: Boolean, val experimentOver: Boolean) {

  constructor() : this(-1, "", "", false, false)

  constructor(requestTime: Long, assignment: String?, payload: String?,
              experimentOver: Boolean) : this(requestTime, assignment, payload, assignment != null,
      experimentOver)

  fun isExpired(): Boolean {
    return requestTime < System.currentTimeMillis() - MAX_CACHE_TIME_IN_MILLIS
  }

  private companion object {
    private const val TWENTY_FOUR_HOURS: Long = 86400000
    const val MAX_CACHE_TIME_IN_MILLIS = TWENTY_FOUR_HOURS
  }
}
