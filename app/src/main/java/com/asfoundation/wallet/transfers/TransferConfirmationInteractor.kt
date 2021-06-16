package com.asfoundation.wallet.transfers

import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.asfoundation.wallet.repository.GasPreferenceRepository
import io.reactivex.Single

class TransferConfirmationInteractor(private val sendTransactionInteract: SendTransactionInteract,
                                     private val fetchGasSettingsInteract: FetchGasSettingsInteract,
                                     private val gasPreferencesRepository: GasPreferenceRepository) {

  fun fetchGasSettings(shouldSendToken: Boolean): Single<GasSettings> {
    return fetchGasSettingsInteract.fetch(shouldSendToken)
  }

  fun send(transactionBuilder: TransactionBuilder?): Single<String> {
    return sendTransactionInteract.send(transactionBuilder)
  }

  fun getGasPreferences(): GasSettings {
    return GasSettings(gasPreferencesRepository.getSavedGasPrice(),
        gasPreferencesRepository.getSavedGasLimit())
  }

}
