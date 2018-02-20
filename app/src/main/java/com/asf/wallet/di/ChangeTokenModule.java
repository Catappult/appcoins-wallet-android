package com.asf.wallet.di;

import com.asf.wallet.interact.ChangeTokenEnableInteract;
import com.asf.wallet.interact.DeleteTokenInteract;
import com.asf.wallet.interact.FetchAllTokenInfoInteract;
import com.asf.wallet.repository.TokenRepositoryType;
import com.asf.wallet.viewmodel.TokenChangeCollectionViewModelFactory;
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
