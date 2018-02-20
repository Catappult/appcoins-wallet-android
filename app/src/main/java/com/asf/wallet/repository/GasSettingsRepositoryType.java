package com.asf.wallet.repository;

import com.asf.wallet.entity.GasSettings;
import io.reactivex.Single;

public interface GasSettingsRepositoryType {
  Single<GasSettings> getGasSettings(boolean forTokenTransfer);
}
