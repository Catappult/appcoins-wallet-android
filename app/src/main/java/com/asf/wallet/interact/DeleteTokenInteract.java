package com.asf.wallet.interact;

import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.repository.TokenRepositoryType;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeleteTokenInteract {
  private final TokenRepositoryType tokenRepository;

  public DeleteTokenInteract(TokenRepositoryType tokenRepository) {
    this.tokenRepository = tokenRepository;
  }

  public Completable delete(Wallet wallet, Token token) {
    return tokenRepository.delete(wallet, token)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
