package com.wallet.crypto.trustapp.di;

import com.wallet.crypto.trustapp.interact.SendTransactionInteract;
import com.wallet.crypto.trustapp.repository.PasswordStore;
import com.wallet.crypto.trustapp.repository.TransactionRepositoryType;
import com.wallet.crypto.trustapp.router.GasSettingsRouter;
import com.wallet.crypto.trustapp.viewmodel.ConfirmationViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module public class ConfirmationModule {
  @Provides ConfirmationViewModelFactory provideConfirmationViewModelFactory(
      SendTransactionInteract sendTransactionInteract, GasSettingsRouter gasSettingsRouter) {
    return new ConfirmationViewModelFactory(sendTransactionInteract, gasSettingsRouter);
  }

  @Provides SendTransactionInteract provideSendTransactionInteract(
      TransactionRepositoryType transactionRepository, PasswordStore passwordStore) {
    return new SendTransactionInteract(transactionRepository, passwordStore);
  }

  @Provides GasSettingsRouter provideGasSettingsRouter() {
    return new GasSettingsRouter();
  }
}
