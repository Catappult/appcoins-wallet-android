package com.asf.wallet.interact;

import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.repository.TokenRepositoryType;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.Arrays;

public class FetchTokensInteract {

  private final TokenRepositoryType tokenRepository;
  private final DefaultTokenProvider defaultTokenProvider;

  public FetchTokensInteract(TokenRepositoryType tokenRepository,
      DefaultTokenProvider defaultTokenProvider) {
    this.tokenRepository = tokenRepository;
    this.defaultTokenProvider = defaultTokenProvider;
  }

  public Observable<Token[]> fetch(Wallet wallet) {
    return tokenRepository.fetchActive(wallet.address)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  public Observable<Token> fetchDefaultToken(Wallet wallet) {
    return tokenRepository.fetchActive(wallet.address)
        .flatMap(tokens -> defaultTokenProvider.getDefaultToken()
            .flatMapObservable(defaultToken -> Observable.fromIterable(Arrays.asList(tokens))
                .filter(token -> token.tokenInfo.symbol.equals(defaultToken))))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
