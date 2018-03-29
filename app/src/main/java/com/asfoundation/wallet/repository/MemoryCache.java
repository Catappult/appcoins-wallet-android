package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by trinkes on 3/15/18.
 */

public class MemoryCache<K, V> implements Cache<K, V> {
  private final BehaviorSubject<Map<K, V>> subject;
  private final Map<K, V> cache;

  public MemoryCache(BehaviorSubject<Map<K, V>> subject, Map<K, V> cache) {
    this.subject = subject;
    this.cache = cache;
  }

  @Override public Completable save(K key, V value) {
    return Completable.fromAction(() -> {
      cache.put(key, value);
      subject.onNext(new HashMap<>(cache));
    });
  }

  @Override public Observable<List<V>> getAll() {
    return subject.map(itemsMap -> new ArrayList<>(itemsMap.values()));
  }

  @Override public Observable<V> get(K key) {
    return subject.flatMap(items -> Observable.fromIterable(items.entrySet())
        .filter(item -> item.getKey()
            .equals(key))
        .map(Map.Entry::getValue));
  }

  @Override public Completable remove(K key) {
    return Completable.fromAction(() -> {
      cache.remove(key);
      subject.onNext(new HashMap<>(cache));
    });
  }

  @Override public Single<Boolean> contains(K key) {
    return Single.just(cache.containsKey(key));
  }
}
