package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ConfirmationRouter;
import com.asfoundation.wallet.util.TransferParser;
import com.asfoundation.wallet.viewmodel.SendViewModelFactory;
import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.PublishSubject;

@Module public class SendModule {
  @Provides SendViewModelFactory provideSendViewModelFactory(
      FindDefaultWalletInteract findDefaultWalletInteract, ConfirmationRouter confirmationRouter,
      FetchGasSettingsInteract fetchGasSettingsInteract, TransferParser transferParser) {
    return new SendViewModelFactory(findDefaultWalletInteract, fetchGasSettingsInteract,
        confirmationRouter, transferParser);
  }

  @Provides ConfirmationRouter provideConfirmationRouter() {
    return new ConfirmationRouter(PublishSubject.create());
  }

}
