package com.asf.wallet.di;

import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.TransactionRepositoryType;
import com.asf.wallet.router.GasSettingsRouter;
import com.asf.wallet.viewmodel.ConfirmationViewModelFactory;
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
