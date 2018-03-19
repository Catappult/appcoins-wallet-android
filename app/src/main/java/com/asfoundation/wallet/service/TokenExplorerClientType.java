package com.asfoundation.wallet.service;

import com.asfoundation.wallet.entity.TokenInfo;
import io.reactivex.Observable;

public interface TokenExplorerClientType {
  Observable<TokenInfo[]> fetch(String walletAddress);
}