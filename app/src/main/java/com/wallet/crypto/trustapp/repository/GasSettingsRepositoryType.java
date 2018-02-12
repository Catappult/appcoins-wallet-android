package com.wallet.crypto.trustapp.repository;

import com.wallet.crypto.trustapp.entity.GasSettings;
import io.reactivex.Single;

public interface GasSettingsRepositoryType {
  Single<GasSettings> getGasSettings(boolean forTokenTransfer);
}
