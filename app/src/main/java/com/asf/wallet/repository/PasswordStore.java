package com.asf.wallet.repository;

import com.asf.wallet.entity.Wallet;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface PasswordStore {
  Single<String> getPassword(Wallet wallet);

  Completable setPassword(Wallet wallet, String password);

  Single<String> generatePassword();
}
