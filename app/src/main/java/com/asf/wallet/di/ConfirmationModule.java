package com.asf.wallet.di;

import com.asf.wallet.interact.FetchGasSettingsInteract;
import com.asf.wallet.interact.FindDefaultWalletInteract;
import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.PendingTransactionService;
import com.asf.wallet.repository.TransactionRepositoryType;
import com.asf.wallet.repository.TransactionService;
import com.asf.wallet.router.GasSettingsRouter;
import com.asf.wallet.util.TransferParser;
import com.asf.wallet.viewmodel.ConfirmationViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module(includes = { SendModule.class}) public class ConfirmationModule {

  @Provides ConfirmationViewModelFactory provideConfirmationViewModelFactory(
      SendTransactionInteract sendTransactionInteract, GasSettingsRouter gasSettingsRouter,
      PendingTransactionService pendingTransactionService) {
    return new ConfirmationViewModelFactory(sendTransactionInteract, gasSettingsRouter,
        pendingTransactionService);
  }

  @Provides SendTransactionInteract provideSendTransactionInteract(
      TransactionRepositoryType transactionRepository, PasswordStore passwordStore) {
    return new SendTransactionInteract(transactionRepository, passwordStore);
  }

  @Provides GasSettingsRouter provideGasSettingsRouter() {
    return new GasSettingsRouter();
  }

  @Provides TransactionService provideTransactionService(
      FetchGasSettingsInteract gasSettingsInteract, SendTransactionInteract sendTransactionInteract,
      PendingTransactionService pendingTransactionService, TransferParser parser,
      FindDefaultWalletInteract defaultWalletInteract) {
    return new TransactionService(gasSettingsInteract, sendTransactionInteract,
        pendingTransactionService, defaultWalletInteract, parser);
  }
}
