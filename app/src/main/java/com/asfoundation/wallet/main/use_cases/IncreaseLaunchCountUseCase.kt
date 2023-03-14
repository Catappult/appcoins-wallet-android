package com.asfoundation.wallet.main.use_cases

import repository.CommonsPreferencesDataSource
import javax.inject.Inject

class IncreaseLaunchCountUseCase @Inject constructor(
  val commonsPreferencesDataSource: CommonsPreferencesDataSource
) {

  companion object {
    // An high arbitrary number that conceivably will cover any future use case
    private const val MAX_NUMBER_OF_TIMES = 100
  }

  operator fun invoke() {
    if (commonsPreferencesDataSource.getNumberOfTimesOnHome() <= MAX_NUMBER_OF_TIMES) {
      commonsPreferencesDataSource.increaseTimesOnHome()
    }
  }
}