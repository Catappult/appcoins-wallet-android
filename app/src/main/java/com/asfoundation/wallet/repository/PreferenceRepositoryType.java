package com.asfoundation.wallet.repository;

public interface PreferenceRepositoryType {
  boolean hasAcceptedTCAndPP();

  void setAcceptedTCAndPP();

  String getCurrentWalletAddress();

  void setCurrentWalletAddress(String address);

  String getDefaultNetwork();

  void setDefaultNetwork(String netName);
}
