package com.appcoins.wallet.core.utils.android_common

import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

interface Dispatchers {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
  val computation: CoroutineDispatcher
}

@Singleton
class DispatchersImpl @Inject constructor() : Dispatchers {
  override val main: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Main
  override val io: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
  override val computation: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default
}