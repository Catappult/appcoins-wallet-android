package com.asfoundation.wallet.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.asfoundation.wallet.ui.iab.raiden.RaidenRepository;

public class SharedPreferenceRepository implements PreferenceRepositoryType, RaidenRepository {

  private static final String CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address";
  private static final String DEFAULT_NETWORK_NAME_KEY = "default_network_name";
  private static final String GAS_PRICE_KEY = "gas_price";
  private static final String GAS_LIMIT_KEY = "gas_limit";
  private static final String GAS_LIMIT_FOR_TOKENS_KEY = "gas_limit_for_tokens";
  private static final String SHOULD_SHOW_RAIDEN_DIALOG = "should_show_raiden_dialog";

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

  @Override public String getDefaultNetwork() {
    return pref.getString(DEFAULT_NETWORK_NAME_KEY, null);
  }

  @Override public void setDefaultNetwork(String netName) {
    pref.edit()
        .putString(DEFAULT_NETWORK_NAME_KEY, netName)
        .apply();
  }

  @Override public boolean shouldShowDialog() {
    return pref.getBoolean(SHOULD_SHOW_RAIDEN_DIALOG, true);
  }

  @Override public void setShouldShowDialog(boolean shouldShow) {
    pref.edit()
        .putBoolean(SHOULD_SHOW_RAIDEN_DIALOG, shouldShow)
        .apply();
  }
}
