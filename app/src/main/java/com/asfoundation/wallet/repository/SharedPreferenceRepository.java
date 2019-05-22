package com.asfoundation.wallet.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceRepository implements PreferenceRepositoryType {

  private static final String CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address";
  private static final String ONBOARDING_COMPLETE_KEY = "onboarding_complete";
  private static final String ONBOARDING_SKIP_CLICKED_KEY = "onboarding_skip_clicked";

  private final SharedPreferences pref;

  public SharedPreferenceRepository(Context context) {
    pref = PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Override public boolean hasCompletedOnboarding() {
    return pref.getBoolean(ONBOARDING_COMPLETE_KEY, false);
  }

  @Override public void setOnboardingComplete() {
    pref.edit()
        .putBoolean(ONBOARDING_COMPLETE_KEY, true)
        .apply();
  }

  @Override public boolean hasClickedSkipOnboarding() {
    return pref.getBoolean(ONBOARDING_SKIP_CLICKED_KEY, false);
  }

  @Override public void setOnboardingSkipClicked() {
    pref.edit()
        .putBoolean(ONBOARDING_SKIP_CLICKED_KEY, true)
        .apply();
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
