package com.appcoins.wallet.commons

import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by trinkes on 3/15/18.
 */
class MemoryCacheTest {

  private var cache: MemoryCache<Int, Int>? = null

  @Before
  fun before() {
    cache = MemoryCache(
      BehaviorSubject.create(),
      ConcurrentHashMap()
    )
  }

  @Test
  fun add() {
    cache!!.save(1, 2)
        .subscribe()
    val testObserver = TestObserver<List<Int>>()
    cache!!.all
        .subscribe(testObserver)
    Assert.assertEquals(testObserver.valueCount().toLong(), 1)
    Assert.assertEquals(testObserver.values()[0][0], 2)
  }

  @Test
  fun getAll() {
    cache!!.save(1, 2)
        .subscribe()
    val testObserver = TestObserver<List<Int>>()
    cache!!.all
        .subscribe(testObserver)
    Assert.assertEquals(testObserver.valueCount().toLong(), 1)
    Assert.assertEquals(testObserver.values()[0][0], 2)
    Assert.assertEquals(testObserver.values()[0].size.toLong(), 1)
    cache!!.save(3, 4)
        .subscribe()
    Assert.assertEquals(testObserver.valueCount().toLong(), 2)
    Assert.assertEquals(testObserver.values()[1][0], 2)
    Assert.assertEquals(testObserver.values()[1][1], 4)
    Assert.assertEquals(testObserver.values()[1].size.toLong(), 2)
  }

  @Test
  fun get() {
    cache!!.save(1, 2)
        .subscribe()
    val testObserver = TestObserver<Int>()
    cache!![1]
        .subscribe(testObserver)
    Assert.assertEquals(testObserver.valueCount().toLong(), 1)
    Assert.assertEquals(testObserver.values()[0], 2)
    cache!!.save(1, 3)
        .subscribe()
    Assert.assertEquals(testObserver.valueCount().toLong(), 2)
    Assert.assertEquals(testObserver.values()[1], 3)
  }

  @Test
  fun remove() {
    cache!!.save(1, 2)
        .subscribe()
    cache!!.remove(1)
        .subscribe()
    val testObserver = TestObserver<Any>()
    cache!!.all
        .subscribe(testObserver)
    Assert.assertEquals(testObserver.valueCount().toLong(), 1)
    val expected = testObserver.values()[0] as ArrayList<Int>
    Assert.assertEquals(expected.size.toLong(), 0)
  }

  @Test
  fun contains() {
    cache!!.save(1, 2)
        .blockingAwait()
    val subscriber = TestObserver<Boolean>()
    cache!!.contains(1)
        .subscribe(subscriber)
    subscriber.assertNoErrors()
        .assertValue(true)
        .assertComplete()
  }

  @Test
  fun containsEmptyCache() {
    val subscriber = TestObserver<Boolean>()
    cache!!.contains(1)
        .subscribe(subscriber)
    subscriber.assertNoErrors()
        .assertValue(false)
        .assertComplete()
  }

  @Test
  fun notContains() {
    cache!!.save(1, 2)
        .blockingAwait()
    val subscriber = TestObserver<Boolean>()
    cache!!.contains(2)
        .subscribe(subscriber)
    subscriber.assertNoErrors()
        .assertValue(false)
        .assertComplete()
  }

  @Test
  fun save() {
    cache!!.save(1, 2)
        .blockingAwait()
    val subscriber = TestObserver<Boolean>()
    cache!!.contains(1)
        .subscribe(subscriber)
    subscriber.assertNoErrors()
        .assertValue(true)
        .assertComplete()
  }

  @Test
  fun saveSync() {
    cache!!.saveSync(1, 2)
    val subscriber = TestObserver<Boolean>()
    cache!!.contains(1)
        .subscribe(subscriber)
    subscriber.assertNoErrors()
        .assertValue(true)
        .assertComplete()
  }

  @Test
  fun getAllSync() {
    cache!!.save(1, 2)
        .subscribe()
    var values = cache!!.allSync
    Assert.assertEquals(values.size.toLong(), 1)
    Assert.assertEquals(values[0], 2)
    Assert.assertEquals(values.size.toLong(), 1)
    cache!!.save(3, 4)
        .subscribe()
    values = cache!!.allSync
    Assert.assertEquals(values.size.toLong(), 2)
    Assert.assertEquals(values[0], 2)
    Assert.assertEquals(values[1], 4)
    Assert.assertEquals(values.size.toLong(), 2)
  }

  @Test
  fun getSync() {
    cache!!.save(1, 2)
        .subscribe()
    var value = cache!!.getSync(1)
    Assert.assertEquals(value, 2)
    cache!!.save(1, 3)
        .subscribe()
    value = cache!!.getSync(1)
    Assert.assertEquals(value, 3)
  }

  @Test
  fun removeSync() {
    cache!!.save(1, 2)
        .subscribe()
    cache!!.removeSync(1)
    val testObserver = TestObserver<Any>()
    cache!!.all
        .subscribe(testObserver)
    Assert.assertEquals(testObserver.valueCount().toLong(), 1)
    val expected = testObserver.values()[0] as ArrayList<Int>
    Assert.assertEquals(expected.size.toLong(), 0)
  }

  @Test
  fun containsSync() {
    cache!!.save(1, 2)
        .blockingAwait()
    var value = cache!!.containsSync(1)
    Assert.assertEquals(true, value)
    cache!!.removeSync(1)
    value = cache!!.containsSync(1)
    Assert.assertEquals(false, value)
  }
}