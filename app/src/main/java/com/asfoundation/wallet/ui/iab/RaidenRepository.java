package com.asfoundation.wallet.ui.iab;

public interface RaidenRepository {
  boolean shouldShowDialog();

  void setShouldShowDialog(boolean shouldShow);
}
