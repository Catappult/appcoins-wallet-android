package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

/**
 * Created by trinkes on 3/15/18.
 */

public interface Cache<K, T> {
  Completable save(K key, T value);

  Observable<List<T>> getAll();

  Observable<T> get(K key);

  Completable remove(K key);

  Single<Boolean> contains(K key);
}
