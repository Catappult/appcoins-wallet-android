package com.asf.wallet.interact;

import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.repository.TokenRepositoryType;
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
