package com.appcoins.wallet.ui.arch.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

/**
 * Specification of operations result throughout the architecture stack.
 * Once we reach ViewModel / ViewState, we can map this to [Async]
 */
typealias DataResult<T> = Result<T, Error>

inline infix fun <V> V?.toDataResultOr(error: () -> Error): Result<V, Error> {
  return when (this) {
    null -> Err(error())
    else -> Ok(this)
  }
}

fun <V> V?.toDataResult(): DataResult<V> {
  return when (this) {
    null -> Err(Error.NotFoundError(NullPointerException()))
    else -> Ok(this)
  }
}