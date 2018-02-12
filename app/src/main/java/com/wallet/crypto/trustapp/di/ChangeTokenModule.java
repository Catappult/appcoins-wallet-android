package com.wallet.crypto.trustapp.di;

import com.wallet.crypto.trustapp.interact.ChangeTokenEnableInteract;
import com.wallet.crypto.trustapp.interact.DeleteTokenInteract;
import com.wallet.crypto.trustapp.interact.FetchAllTokenInfoInteract;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
import com.wallet.crypto.trustapp.viewmodel.TokenChangeCollectionViewModelFactory;
import dagger.Module;
import dagger.Provides;

@Module class ChangeTokenModule {

  @Provides TokenChangeCollectionViewModelFactory provideChangeTokenCollectionViewModelFactory(
      FetchAllTokenInfoInteract fetchAllTokenInfoInteract,
      ChangeTokenEnableInteract changeTokenEnableInteract,
      DeleteTokenInteract deleteTokenInteract) {
    return new TokenChangeCollectionViewModelFactory(fetchAllTokenInfoInteract,
        changeTokenEnableInteract, deleteTokenInteract);
  }

  @Provides FetchAllTokenInfoInteract provideFetchAllTokenInfoInteract(
      TokenRepositoryType tokenRepository) {
    return new FetchAllTokenInfoInteract(tokenRepository);
  }

  @Provides ChangeTokenEnableInteract provideChangeTokenEnableInteract(
      TokenRepositoryType tokenRepository) {
    return new ChangeTokenEnableInteract(tokenRepository);
  }

  @Provides DeleteTokenInteract provideDeleteTokenInteract(TokenRepositoryType tokenRepository) {
    return new DeleteTokenInteract(tokenRepository);
  }
}
