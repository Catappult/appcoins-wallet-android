package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.GasSettings;
import io.reactivex.Single;

public interface GasSettingsRepositoryType {
  Single<GasSettings> getGasSettings(boolean forTokenTransfer);
}
