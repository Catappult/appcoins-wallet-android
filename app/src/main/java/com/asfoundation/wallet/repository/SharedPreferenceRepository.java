package com.asfoundation.wallet.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceRepository implements PreferenceRepositoryType {

  private static final String CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address";

  private final SharedPreferences pref;

  public SharedPreferenceRepository(Context context) {
    pref = PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Override public String getCurrentWalletAddress() {
    return pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null);
  }

  @Override public void setCurrentWalletAddress(String address) {
    pref.edit()
        .putString(CURRENT_ACCOUNT_ADDRESS_KEY, address)
        .apply();
  }
}
