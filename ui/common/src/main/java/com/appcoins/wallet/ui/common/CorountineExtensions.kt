package com.appcoins.wallet.ui.common

import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun <T> Single<T>.callAsync(
  coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
): T = coroutineScope { async(coroutineDispatcher, block = { this@callAsync.blockingGet() }).await() }

suspend fun <T> Observable<T>.callAsync(
  coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
): T = Single.fromObservable(this).callAsync(coroutineDispatcher)