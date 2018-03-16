package com.asf.wallet.di;

import com.asf.wallet.interact.SendTransactionInteract;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.PendingTransactionService;
import com.asf.wallet.repository.TransactionRepositoryType;
import com.asf.wallet.router.GasSettingsRouter;
import com.asf.wallet.viewmodel.ConfirmationViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module(includes = { SendModule.class }) public class ConfirmationModule {

  @Provides ConfirmationViewModelFactory provideConfirmationViewModelFactory(
      SendTransactionInteract sendTransactionInteract, GasSettingsRouter gasSettingsRouter,
      PendingTransactionService pendingTransactionService) {
    return new ConfirmationViewModelFactory(sendTransactionInteract, gasSettingsRouter,
        pendingTransactionService);
  }


}
