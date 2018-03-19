package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchTokensInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.router.AddTokenRouter;
import com.asfoundation.wallet.router.ChangeTokenCollectionRouter;
import com.asfoundation.wallet.router.SendRouter;
import com.asfoundation.wallet.router.TransactionsRouter;
import com.asfoundation.wallet.viewmodel.TokensViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module class TokensModule {

  @Provides TokensViewModelFactory provideTokensViewModelFactory(
      FetchTokensInteract fetchTokensInteract, AddTokenRouter addTokenRouter,
      SendRouter sendTokenRouter, TransactionsRouter transactionsRouter,
      ChangeTokenCollectionRouter changeTokenCollectionRouter) {
    return new TokensViewModelFactory(fetchTokensInteract, addTokenRouter, sendTokenRouter,
        transactionsRouter, changeTokenCollectionRouter);
  }

  @Provides FindDefaultNetworkInteract provideFindDefaultNetworkInteract(
      EthereumNetworkRepositoryType networkRepository) {
    return new FindDefaultNetworkInteract(networkRepository);
  }

  @Provides FetchTokensInteract provideFetchTokensInteract(TokenRepositoryType tokenRepository) {
    return new FetchTokensInteract(tokenRepository, new BuildConfigDefaultTokenProvider());
  }

  @Provides AddTokenRouter provideAddTokenRouter() {
    return new AddTokenRouter();
  }

  @Provides SendRouter provideSendTokenRouter() {
    return new SendRouter();
  }

  @Provides TransactionsRouter provideTransactionsRouter() {
    return new TransactionsRouter();
  }

  @Provides ChangeTokenCollectionRouter provideChangeTokenCollectionRouter() {
    return new ChangeTokenCollectionRouter();
  }
}
