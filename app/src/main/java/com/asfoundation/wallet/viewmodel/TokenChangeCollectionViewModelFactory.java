package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.asfoundation.wallet.interact.ChangeTokenEnableInteract;
import com.asfoundation.wallet.interact.DeleteTokenInteract;
import com.asfoundation.wallet.interact.FetchAllTokenInfoInteract;

public class TokenChangeCollectionViewModelFactory implements ViewModelProvider.Factory {

  private final FetchAllTokenInfoInteract fetchAllTokenInfoInteract;
  private final DeleteTokenInteract deleteTokenInteract;
  private final ChangeTokenEnableInteract changeTokenEnableInteract;

  public TokenChangeCollectionViewModelFactory(FetchAllTokenInfoInteract fetchAllTokenInfoInteract,
      ChangeTokenEnableInteract changeTokenEnableInteract,
      DeleteTokenInteract deleteTokenInteract) {
    this.fetchAllTokenInfoInteract = fetchAllTokenInfoInteract;
    this.deleteTokenInteract = deleteTokenInteract;
    this.changeTokenEnableInteract = changeTokenEnableInteract;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new TokenChangeCollectionViewModel(fetchAllTokenInfoInteract,
        changeTokenEnableInteract, deleteTokenInteract);
  }
}
