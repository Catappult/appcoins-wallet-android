package com.wallet.crypto.trustapp.repository;

import com.wallet.crypto.trustapp.entity.Token;

import io.reactivex.Observable;

public interface TokenRepositoryType {

    Observable<Token[]> fetch(String walletAddress);
}
