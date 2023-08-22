package com.appcoins.wallet.core.utils.android_common

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean


class LiveEvent<T> : MediatorLiveData<T>() {

  private val observers = ConcurrentHashMap<LifecycleOwner, MutableSet<ObserverWrapper<T>>>()

  @MainThread
  override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
    val wrapper = ObserverWrapper(observer as Observer<T>)
    val set = observers[owner]
    set?.apply {
      add(wrapper)
    } ?: run {
      val newSet = Collections.newSetFromMap(ConcurrentHashMap<ObserverWrapper<T>, Boolean>())
      newSet.add(wrapper)
      observers[owner] = newSet
    }
    super.observe(owner, wrapper)
  }

  override fun removeObservers(owner: LifecycleOwner) {
    observers.remove(owner)
    super.removeObservers(owner)
  }

  override fun removeObserver(observer: Observer<in T>) {
    observers.forEach {
      if (it.value.remove(observer as Observer<T>)) {
        if (it.value.isEmpty()) {
          observers.remove(it.key)
        }
        return@forEach
      }
    }
    super.removeObserver(observer)
  }

  @MainThread
  override fun setValue(t: T?) {
    observers.forEach { it.value.forEach { wrapper -> wrapper.newValue() } }
    super.setValue(t)
  }

  /**
   * Used for cases where T is Void, to make calls cleaner.
   */
  @MainThread
  fun call() {
    value = null
  }

  private class ObserverWrapper<T>(private val observer: Observer<T>) : Observer<T> {

    private val pending = AtomicBoolean(false)

    override fun onChanged(value: T) {
      if (pending.compareAndSet(true, false)) {
        observer.onChanged(value)
      }
    }

    fun newValue() {
      pending.set(true)
    }
  }
}

fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
  val result = LiveEvent<T>()
  result.addSource(this) {
    result.value = it
  }
  return result
}