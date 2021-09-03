package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface PasswordStore {
  Single<String> getPassword(String address);

  Completable setPassword(String address, String password);

  Single<String> generatePassword();

  Completable setBackUpPassword(String masterPassword);
}
