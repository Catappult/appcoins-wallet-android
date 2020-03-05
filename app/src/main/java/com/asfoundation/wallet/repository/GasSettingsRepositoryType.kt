package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.GasSettings
import io.reactivex.Single

interface GasSettingsRepositoryType {
  fun getGasSettings(forTokenTransfer: Boolean): Single<GasSettings>
}