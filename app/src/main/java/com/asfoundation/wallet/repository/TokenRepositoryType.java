package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Completable;
import io.reactivex.Observable;

public interface TokenRepositoryType {

  Observable<Token[]> fetchActive(String walletAddress);

  Observable<Token[]> fetchAll(String walletAddress);

  Completable addToken(Wallet wallet, String address, String symbol, int decimals,
      boolean isAddedManually);

  Completable setEnable(Wallet wallet, Token token, boolean isEnabled);

  Completable delete(Wallet wallet, Token token);
}
