package com.asf.wallet.service;

import com.asf.wallet.entity.TokenInfo;
import io.reactivex.Observable;

public interface TokenExplorerClientType {
  Observable<TokenInfo[]> fetch(String walletAddress);
}