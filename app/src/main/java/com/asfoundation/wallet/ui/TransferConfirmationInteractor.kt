package com.asfoundation.wallet.ui

import android.util.Pair
import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.SendTransactionInteract
import com.asfoundation.wallet.repository.GasPreferenceRepository
import io.reactivex.Single
import java.math.BigDecimal

class TransferConfirmationInteractor(private val sendTransactionInteract: SendTransactionInteract,
                                     private val fetchGasSettingsInteract: FetchGasSettingsInteract,
                                     private val gasPreferencesRepository: GasPreferenceRepository) {

  fun fetch(shouldSendToken: Boolean): Single<GasSettings> {
    return fetchGasSettingsInteract.fetch(shouldSendToken)
  }

  fun send(transactionBuilder: TransactionBuilder?): Single<String> {
    return sendTransactionInteract.send(transactionBuilder)
  }

  fun getGasPreferences(): Pair<BigDecimal?, BigDecimal?> {
    return Pair(gasPreferencesRepository.getSavedGasPrice(),
        gasPreferencesRepository.getSavedGasLimit())
  }

}
