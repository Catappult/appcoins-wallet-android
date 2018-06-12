package com.asfoundation.wallet.ui.iab.raiden;

public interface RaidenRepository {
  boolean shouldShowDialog();

  void setShouldShowDialog(boolean shouldShow);
}
