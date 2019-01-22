package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.FetchGasSettingsInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.ConfirmationRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import com.asfoundation.wallet.util.TransferParser;
import com.asfoundation.wallet.viewmodel.SendViewModelFactory;
import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.PublishSubject;

@Module public class SendModule {
  @Provides SendViewModelFactory provideSendViewModelFactory(
      FindDefaultWalletInteract findDefaultWalletInteract, ConfirmationRouter confirmationRouter,
      FetchGasSettingsInteract fetchGasSettingsInteract, TransferParser transferParser,
      TransactionsRouter transactionsRouter) {
    return new SendViewModelFactory(findDefaultWalletInteract, fetchGasSettingsInteract,
        confirmationRouter, transferParser, transactionsRouter);
  }

  @Provides ConfirmationRouter provideConfirmationRouter() {
    return new ConfirmationRouter(PublishSubject.create());
  }

  @Provides TransactionsRouter provideTransactionsRouter() {
    return new TransactionsRouter();
  }
}
