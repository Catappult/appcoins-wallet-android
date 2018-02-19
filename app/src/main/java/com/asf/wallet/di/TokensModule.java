package com.asf.wallet.di;

import com.asf.wallet.interact.BuildConfigDefaultTokenProvider;
import com.asf.wallet.interact.FetchTokensInteract;
import com.asf.wallet.interact.FindDefaultNetworkInteract;
import com.asf.wallet.repository.EthereumNetworkRepositoryType;
import com.asf.wallet.repository.TokenRepositoryType;
import com.asf.wallet.router.AddTokenRouter;
import com.asf.wallet.router.ChangeTokenCollectionRouter;
import com.asf.wallet.router.SendRouter;
import com.asf.wallet.router.TransactionsRouter;
import com.asf.wallet.viewmodel.TokensViewModelFactory;
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
