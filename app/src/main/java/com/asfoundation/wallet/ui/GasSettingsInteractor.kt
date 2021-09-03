package com.asfoundation.wallet.ui

import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.repository.GasPreferenceRepository
import io.reactivex.Single
import java.math.BigDecimal

class GasSettingsInteractor(private val findDefaultNetworkInteract: FindDefaultNetworkInteract,
                            private val gasPreferencesRepository: GasPreferenceRepository) {

  fun findDefaultNetwork(): Single<NetworkInfo> = findDefaultNetworkInteract.find()

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
