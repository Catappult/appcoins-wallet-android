package com.wallet.crypto.trustapp.repository;

import com.wallet.crypto.trustapp.entity.Token;

import io.reactivex.Single;

public interface TokenLocalSource {
    Single<Token[]> fetch(String walletAddress);
}
