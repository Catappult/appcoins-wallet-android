package com.appcoins.wallet.ui.arch.data

/**
 * Sealed class that represents the state of an asynchronous operation on View State.
 *
 * It's a very common pattern to execute and observe the state of an asynchronous operation
 * (e.g. network call, database fetch...). This sealed class represents the common states of these
 * operations.
 */
sealed class Async<out T>(val value: T? = null) {

  open operator fun invoke(): T? = value

  object Uninitialized : Async<Nothing>()

  data class Loading<out T>(private val previousValue: T? = null) :
      Async<T>(previousValue)

  /**
   * Previous value should not be here, however due to the way our architecture is setup, this is
   * currently needed for certain cases (e.g. offline first flows).
   */
  data class Success<out T>(private val currentValue: T, val previousValue: T? = null) :
      Async<T>(currentValue) {
    override operator fun invoke(): T = currentValue
  }

  data class Fail<out T>(val error: Error, private val previousValue: T? = null) :
      Async<T>(previousValue)
}