package com.asf.wallet.interact;

import com.asf.wallet.entity.GasSettings;
import com.asf.wallet.repository.GasSettingsRepositoryType;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FetchGasSettingsInteract {
  private final GasSettingsRepositoryType repository;

  public FetchGasSettingsInteract(GasSettingsRepositoryType repository) {
    this.repository = repository;
  }

  public Single<GasSettings> fetch(boolean forTokenTransfer) {
    return repository.getGasSettings(forTokenTransfer)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
