package com.asfoundation.wallet.repository;

public interface PreferenceRepositoryType {
  boolean hasCompletedOnboarding();

  void setOnboardingComplete();

  boolean hasClickedSkipOnboarding();

  void setOnboardingSkipClicked();

  String getCurrentWalletAddress();

  void setCurrentWalletAddress(String address);
}
