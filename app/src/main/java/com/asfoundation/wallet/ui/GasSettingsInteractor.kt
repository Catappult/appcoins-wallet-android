package com.asfoundation.wallet.ui

import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.home.usecases.FindNetworkInfoUseCase
import io.reactivex.Single
import repository.GasPreferencesDataSource
import java.math.BigDecimal
import javax.inject.Inject

class GasSettingsInteractor @Inject constructor(
  private val findNetworkInfoUseCase: FindNetworkInfoUseCase,
  private val gasPreferencesRepository: GasPreferencesDataSource
) {

  fun findDefaultNetwork(): Single<NetworkInfo> = findNetworkInfoUseCase()

  fun saveGasPreferences(price: BigDecimal, limit: BigDecimal) {
    val savedGasPrice = gasPreferencesRepository.getSavedGasPrice()
    val savedGasLimit = gasPreferencesRepository.getSavedGasLimit()
    if (savedGasPrice != price) {
      gasPreferencesRepository.saveGasPrice(price)
    }
    if (savedGasLimit != limit) {
      gasPreferencesRepository.saveGasLimit(limit)
    }
  }

  fun getSavedGasPreferences(): GasSettings {
    return GasSettings(gasPreferencesRepository.getSavedGasPrice(),
        gasPreferencesRepository.getSavedGasLimit())
  }
}
