package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.ImportWalletInteract;
import com.asfoundation.wallet.repository.WalletRepositoryType;

public class ImportWalletViewModelFactory implements ViewModelProvider.Factory {

  private final ImportWalletInteract importWalletInteract;
  private final WalletRepositoryType walletRepository;

  public ImportWalletViewModelFactory(ImportWalletInteract importWalletInteract,
      WalletRepositoryType walletRepository) {
    this.importWalletInteract = importWalletInteract;
    this.walletRepository = walletRepository;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new ImportWalletViewModel(importWalletInteract, walletRepository);
  }
}
