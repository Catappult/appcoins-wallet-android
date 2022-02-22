package com.asfoundation.wallet.poa;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TaggedCompositeDisposableTest {

  private TaggedCompositeDisposable compositeDisposable;

  @Before public void before() {
    compositeDisposable = new TaggedCompositeDisposable();
  }

  @Test public void addGet() {
    Disposable disposable = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    String key = "key";
    compositeDisposable.add(key, disposable);

    List<Disposable> disposables = compositeDisposable.get(key);
    Assert.assertEquals(1, disposables.size());
    Assert.assertEquals(disposables.get(0), disposable);
  }

  @Test public void disposeAllDifferentKeys() {
    Disposable disposable1 = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    Disposable disposable2 = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    Disposable disposable3 = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    String key1 = "key1";
    compositeDisposable.add(key1, disposable1);
    String key2 = "key2";
    compositeDisposable.add(key2, disposable2);
    compositeDisposable.add(key2, disposable3);

    compositeDisposable.disposeAll();
    Assert.assertTrue(disposable1.isDisposed());
    Assert.assertTrue(disposable2.isDisposed());
    Assert.assertTrue(disposable3.isDisposed());
  }

  @Test public void disposeAllSameKeys() {
    Disposable disposable1 = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    Disposable disposable2 = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    String key1 = "key1";
    compositeDisposable.add(key1, disposable1);
    compositeDisposable.add(key1, disposable2);

    compositeDisposable.disposeAll();
    Assert.assertTrue(disposable1.isDisposed());
    Assert.assertTrue(disposable2.isDisposed());
  }

  @Test public void dispose() {
    Disposable disposable1 = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    Disposable disposable2 = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    Disposable disposable3 = Observable.interval(2, TimeUnit.SECONDS)
        .subscribe();
    String key1 = "key1";
    compositeDisposable.add(key1, disposable1);
    String key2 = "key2";
    compositeDisposable.add(key2, disposable2);
    compositeDisposable.add(key2, disposable3);

    compositeDisposable.dispose(key2);
    Assert.assertFalse(disposable1.isDisposed());
    Assert.assertTrue(disposable2.isDisposed());
    Assert.assertTrue(disposable3.isDisposed());
  }
}