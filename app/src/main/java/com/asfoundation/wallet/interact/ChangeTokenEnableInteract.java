package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChangeTokenEnableInteract {
  private final TokenRepositoryType tokenRepository;

  public ChangeTokenEnableInteract(TokenRepositoryType tokenRepository) {
    this.tokenRepository = tokenRepository;
  }

  public Completable setEnable(Wallet wallet, Token token) {
    return tokenRepository.setEnable(wallet, token, !token.tokenInfo.isEnabled)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
