package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.SendTransactionInteract;
import com.asfoundation.wallet.router.GasSettingsRouter;
import com.asfoundation.wallet.viewmodel.ConfirmationViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module(includes = { SendModule.class }) public class ConfirmationModule {

  @Provides ConfirmationViewModelFactory provideConfirmationViewModelFactory(
      SendTransactionInteract sendTransactionInteract, GasSettingsRouter gasSettingsRouter,
      FetchGasSettingsInteract gasSettingsInteract) {
    return new ConfirmationViewModelFactory(sendTransactionInteract, gasSettingsRouter,
        gasSettingsInteract);
  }
}
