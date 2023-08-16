package com.appcoins.wallet.core.network.eskills.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appComingSoonRegistration") public class RoomAppComingSoonRegistration {

  @NonNull @PrimaryKey private final String packageName;

  public RoomAppComingSoonRegistration(@NonNull String packageName) {
    this.packageName = packageName;
  }

  @NonNull public String getPackageName() {
    return packageName;
  }
}
