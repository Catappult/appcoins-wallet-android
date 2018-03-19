package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FetchAllTokenInfoInteract {
  private final TokenRepositoryType tokenRepository;

  public FetchAllTokenInfoInteract(TokenRepositoryType tokenRepository) {
    this.tokenRepository = tokenRepository;
  }

  public Observable<Token[]> fetch(Wallet wallet) {
    return tokenRepository.fetchAll(wallet.address)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
