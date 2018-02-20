package com.asf.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.asf.wallet.interact.ChangeTokenEnableInteract;
import com.asf.wallet.interact.DeleteTokenInteract;
import com.asf.wallet.interact.FetchAllTokenInfoInteract;

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
