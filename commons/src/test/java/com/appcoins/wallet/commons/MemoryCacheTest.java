package com.appcoins.wallet.commons;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by trinkes on 3/15/18.
 */
public class MemoryCacheTest {
  private MemoryCache<Integer, Integer> cache;

  @Before public void before() {
    cache = new MemoryCache<>(BehaviorSubject.create(), new ConcurrentHashMap<>());
  }

  @Test public void add() {
    cache.save(1, 2)
        .subscribe();
    TestObserver<List<Integer>> testObserver = new TestObserver<>();
    cache.getAll()
        .subscribe(testObserver);
    Assert.assertEquals(testObserver.valueCount(), 1);
    Assert.assertEquals(testObserver.values()
        .get(0)
        .get(0), new Integer(2));
  }

  @Test public void getAll() {
    cache.save(1, 2)
        .subscribe();
    TestObserver<List<Integer>> testObserver = new TestObserver<>();
    cache.getAll()
        .subscribe(testObserver);
    Assert.assertEquals(testObserver.valueCount(), 1);
    Assert.assertEquals(testObserver.values()
        .get(0)
        .get(0), new Integer(2));
    Assert.assertEquals(testObserver.values()
        .get(0)
        .size(), 1);

    cache.save(3, 4)
        .subscribe();
    Assert.assertEquals(testObserver.valueCount(), 2);
    Assert.assertEquals(testObserver.values()
        .get(1)
        .get(0), new Integer(2));
    Assert.assertEquals(testObserver.values()
        .get(1)
        .get(1), new Integer(4));
    Assert.assertEquals(testObserver.values()
        .get(1)
        .size(), 2);
  }

  @Test public void get() {
    cache.save(1, 2)
        .subscribe();
    TestObserver<Integer> testObserver = new TestObserver<>();
    cache.get(1)
        .subscribe(testObserver);

    Assert.assertEquals(testObserver.valueCount(), 1);
    Assert.assertEquals(testObserver.values()
        .get(0), new Integer(2));

    cache.save(1, 3)
        .subscribe();

    Assert.assertEquals(testObserver.valueCount(), 2);
    Assert.assertEquals(testObserver.values()
        .get(1), new Integer(3));
  }

  @Test public void remove() {
    cache.save(1, 2)
        .subscribe();
    cache.remove(1)
        .subscribe();
    TestObserver<Object> testObserver = new TestObserver<>();
    cache.getAll()
        .subscribe(testObserver);

    Assert.assertEquals(testObserver.valueCount(), 1);
    ArrayList<Integer> expected = (ArrayList<Integer>) testObserver.values().<Integer>get(0);
    Assert.assertEquals(expected.size(), 0);
  }

  @Test public void contains() {
    cache.save(1, 2)
        .blockingAwait();
    TestObserver<Boolean> subscriber = new TestObserver<>();
    cache.contains(1)
        .subscribe(subscriber);
    subscriber.assertNoErrors()
        .assertValue(true)
        .assertComplete();
  }

  @Test public void containsEmptyCache() {
    TestObserver<Boolean> subscriber = new TestObserver<>();
    cache.contains(1)
        .subscribe(subscriber);
    subscriber.assertNoErrors()
        .assertValue(false)
        .assertComplete();
  }

  @Test public void notContains() {
    cache.save(1, 2)
        .blockingAwait();
    TestObserver<Boolean> subscriber = new TestObserver<>();
    cache.contains(2)
        .subscribe(subscriber);
    subscriber.assertNoErrors()
        .assertValue(false)
        .assertComplete();
  }

  @Test public void save() {
    cache.save(1, 2)
        .blockingAwait();
    TestObserver<Boolean> subscriber = new TestObserver<>();
    cache.contains(1)
        .subscribe(subscriber);
    subscriber.assertNoErrors()
        .assertValue(true)
        .assertComplete();
  }

  @Test public void saveSync() {
    cache.saveSync(1, 2);
    TestObserver<Boolean> subscriber = new TestObserver<>();
    cache.contains(1)
        .subscribe(subscriber);
    subscriber.assertNoErrors()
        .assertValue(true)
        .assertComplete();
  }

  @Test public void getAllSync() {
    cache.save(1, 2)
        .subscribe();
    List<Integer> values = cache.getAllSync();
    Assert.assertEquals(values.size(), 1);
    Assert.assertEquals(values.get(0), new Integer(2));
    Assert.assertEquals(values.size(), 1);

    cache.save(3, 4)
        .subscribe();
    values = cache.getAllSync();
    Assert.assertEquals(values.size(), 2);
    Assert.assertEquals(values.get(0), new Integer(2));
    Assert.assertEquals(values.get(1), new Integer(4));
    Assert.assertEquals(values.size(), 2);
  }

  @Test public void getSync() {
    cache.save(1, 2)
        .subscribe();
    Integer value = cache.getSync(1);

    Assert.assertEquals(value, new Integer(2));

    cache.save(1, 3)
        .subscribe();
    value = cache.getSync(1);
    Assert.assertEquals(value, new Integer(3));
  }

  @Test public void removeSync() {
    cache.save(1, 2)
        .subscribe();
    cache.removeSync(1);
    TestObserver<Object> testObserver = new TestObserver<>();
    cache.getAll()
        .subscribe(testObserver);

    Assert.assertEquals(testObserver.valueCount(), 1);
    ArrayList<Integer> expected = (ArrayList<Integer>) testObserver.values().<Integer>get(0);
    Assert.assertEquals(expected.size(), 0);
  }

  @Test public void containsSync() {
    cache.save(1, 2)
        .blockingAwait();
    Boolean value = cache.containsSync(1);
    Assert.assertEquals(true, value);

    cache.removeSync(1);
    value = cache.containsSync(1);
    Assert.assertEquals(false, value);
  }
}