package com.asf.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

/**
 * Created by trinkes on 3/15/18.
 */

interface Cache<T, K> {
  Completable save(T key, K value);

  Observable<List<K>> getAll();

  Observable<K> get(T key);

  Completable remove(T key);
}
