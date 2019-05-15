package com.asfoundation.wallet.repository;

public interface PreferenceRepositoryType {
  boolean hasCompletedOnboarding();

  void setOnboardingComplete();

  String getCurrentWalletAddress();

  void setCurrentWalletAddress(String address);
}
