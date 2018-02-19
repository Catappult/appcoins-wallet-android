package com.asf.wallet.service;

import com.asf.wallet.BuildConfig;
import com.asf.wallet.entity.NetworkInfo;
import com.asf.wallet.entity.Wallet;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import java.util.HashMap;
import java.util.Map;

public class RealmManager {

  private final Map<String, RealmConfiguration> realmConfigurations = new HashMap<>();

  public Realm getRealmInstance(NetworkInfo networkInfo, Wallet wallet) {
    String name = getName(networkInfo, wallet);
    RealmConfiguration config = realmConfigurations.get(name);
    if (config == null) {
      config = new RealmConfiguration.Builder().name(name)
          .schemaVersion(BuildConfig.DB_VERSION)
          .deleteRealmIfMigrationNeeded()
          .build();
      realmConfigurations.put(name, config);
    }
    return Realm.getInstance(config);
  }

  private String getName(NetworkInfo networkInfo, Wallet wallet) {
    return wallet.address + "-" + networkInfo.name + "-db.realm";
  }
}
