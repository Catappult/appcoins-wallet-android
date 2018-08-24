package com.asfoundation.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.asfoundation.wallet.interact.ImportWalletInteract;
import com.asfoundation.wallet.repository.WalletRepository;

public class ImportWalletViewModelFactory implements ViewModelProvider.Factory {

  private final ImportWalletInteract importWalletInteract;
  private final WalletRepository walletRepository;

  public ImportWalletViewModelFactory(ImportWalletInteract importWalletInteract,
      WalletRepository walletRepository) {
    this.importWalletInteract = importWalletInteract;
    this.walletRepository = walletRepository;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new ImportWalletViewModel(importWalletInteract, walletRepository);
  }
}
