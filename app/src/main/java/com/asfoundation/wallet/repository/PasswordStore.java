package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.Wallet;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface PasswordStore {
  Single<String> getPassword(Wallet wallet);

  Completable setPassword(String address, String password);

  Single<String> generatePassword();

  Completable setBackUpPassword(String masterPassword);
}
