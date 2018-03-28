package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.TokenRepositoryType;
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
                .filter(token -> token.tokenInfo.address.equalsIgnoreCase(defaultToken.address))))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
