package com.asfoundation.wallet.ui

import android.util.Pair
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract
import com.asfoundation.wallet.repository.GasPreferenceRepository
import io.reactivex.Single
import java.math.BigDecimal

class GasSettingsInteractor(private val findDefaultNetworkInteract: FindDefaultNetworkInteract,
                            private val gasPreferencesRepository: GasPreferenceRepository) {

  fun find(): Single<NetworkInfo> = findDefaultNetworkInteract.find()

  fun saveGasPreferences(price: BigDecimal, limit: BigDecimal) {
    val savedGasPrice = gasPreferencesRepository.getSavedGasPrice()
    val savedGasLimit = gasPreferencesRepository.getSavedGasLimit()
    if (savedGasPrice != price) {
      gasPreferencesRepository.saveGasPrice(price)
    }
    if (savedGasLimit != savedGasLimit) {
      gasPreferencesRepository.saveGasLimit(limit)
    }
  }

  fun getSavedGasPreferences(): Pair<BigDecimal?, BigDecimal?> {
    return Pair(gasPreferencesRepository.getSavedGasPrice(),
        gasPreferencesRepository.getSavedGasLimit())
  }
}
