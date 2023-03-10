package com.appcoins.wallet.commons;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

/**
 * Created by trinkes on 3/15/18.
 */

public interface Repository<K, T> {
  Completable save(K key, T value);

  Observable<List<T>> getAll();

  Observable<T> get(K key);

  Completable remove(K key);

  Single<Boolean> contains(K key);

  void saveSync(K key, T value);

  List<T> getAllSync();

  T getSync(K key);

  void removeSync(K key);

  boolean containsSync(K key);
}
