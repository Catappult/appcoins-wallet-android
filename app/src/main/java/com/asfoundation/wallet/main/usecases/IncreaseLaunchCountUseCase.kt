package com.asfoundation.wallet.main.usecases

import com.asfoundation.wallet.repository.PreferencesRepositoryType

class IncreaseLaunchCountUseCase(val preferencesRepositoryType: PreferencesRepositoryType) {

  companion object {
    // An high arbitrary number that conceivably will cover any future use case
    private const val MAX_NUMBER_OF_TIMES = 100
  }

  operator fun invoke() {
    if (preferencesRepositoryType.getNumberOfTimesOnHome() <= MAX_NUMBER_OF_TIMES) {
      preferencesRepositoryType.increaseTimesOnHome()
    }
  }
}