package com.wallet.crypto.trustapp.interact;

import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
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
