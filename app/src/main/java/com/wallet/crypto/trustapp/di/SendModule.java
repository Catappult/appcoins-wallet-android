package com.wallet.crypto.trustapp.di;

import com.wallet.crypto.trustapp.interact.FetchGasSettingsInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.repository.GasSettingsRepositoryType;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
import com.wallet.crypto.trustapp.repository.WalletRepositoryType;
import com.wallet.crypto.trustapp.router.ConfirmationRouter;
import com.wallet.crypto.trustapp.util.TransferParser;
import com.wallet.crypto.trustapp.viewmodel.SendViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module class SendModule {
  @Provides SendViewModelFactory provideSendViewModelFactory(
      FindDefaultWalletInteract findDefaultWalletInteract, ConfirmationRouter confirmationRouter,
      FetchGasSettingsInteract fetchGasSettingsInteract, TransferParser transferParser) {
    return new SendViewModelFactory(findDefaultWalletInteract, fetchGasSettingsInteract,
        confirmationRouter, transferParser);
  }

  @Provides ConfirmationRouter provideConfirmationRouter() {
    return new ConfirmationRouter();
  }

  @Provides TransferParser provideTransferParser(
      FindDefaultWalletInteract provideFindDefaultWalletInteract,
      TokenRepositoryType tokenRepositoryType) {
    return new TransferParser(provideFindDefaultWalletInteract, tokenRepositoryType);
  }

  @Provides FetchGasSettingsInteract provideFetchGasSettingsInteract(
      GasSettingsRepositoryType gasSettingsRepository) {
    return new FetchGasSettingsInteract(gasSettingsRepository);
  }

  @Provides FindDefaultWalletInteract provideFindDefaultWalletInteract(
      WalletRepositoryType walletRepository) {
    return new FindDefaultWalletInteract(walletRepository);
  }
}
