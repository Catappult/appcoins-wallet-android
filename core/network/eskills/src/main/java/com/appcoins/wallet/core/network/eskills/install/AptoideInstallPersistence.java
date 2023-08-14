package com.appcoins.wallet.core.network.eskills.install;

import rx.Single;

public interface AptoideInstallPersistence {

  Single<Boolean> isInstalledWithAptoide(String packageName);

  void insert(String packageName);
}
