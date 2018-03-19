package com.asfoundation.wallet.di;

import com.asfoundation.wallet.interact.ChangeTokenEnableInteract;
import com.asfoundation.wallet.interact.DeleteTokenInteract;
import com.asfoundation.wallet.interact.FetchAllTokenInfoInteract;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.viewmodel.TokenChangeCollectionViewModelFactory;
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
