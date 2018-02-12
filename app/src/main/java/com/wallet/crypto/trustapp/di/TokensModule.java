package com.wallet.crypto.trustapp.di;

import com.wallet.crypto.trustapp.interact.BuildConfigDefaultTokenProvider;
import com.wallet.crypto.trustapp.interact.FetchTokensInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultNetworkInteract;
import com.wallet.crypto.trustapp.repository.EthereumNetworkRepositoryType;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
import com.wallet.crypto.trustapp.router.AddTokenRouter;
import com.wallet.crypto.trustapp.router.ChangeTokenCollectionRouter;
import com.wallet.crypto.trustapp.router.SendRouter;
import com.wallet.crypto.trustapp.router.TransactionsRouter;
import com.wallet.crypto.trustapp.viewmodel.TokensViewModelFactory;
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
