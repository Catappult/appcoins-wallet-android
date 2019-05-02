package com.asfoundation.wallet.repository;

public interface PreferenceRepositoryType {
  String getCurrentWalletAddress();

  void setCurrentWalletAddress(String address);
}
