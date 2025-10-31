package com.appcoins.wallet.core.arch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface SingleStateFragmentWithSnackBar<S : ViewState, E : SideEffect> : SingleStateFragment<S, E> {
  fun onSnackBarMessage(message: SnackBarMessage)

  fun BaseViewModelWithSnackBar<S, E>.collectStateAndEvents(
    lifecycle: Lifecycle,
    scope: LifecycleCoroutineScope
  ) {
    stateFlow
      .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
      .onEach { state ->
        onStateChanged(state)
      }
      .launchIn(scope)
    sideEffectsFlow
      .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
      .onEach { sideEffect ->
        onSideEffect(sideEffect)
      }
      .launchIn(scope)
    snackBarMessagesFlow
      .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
      .onEach { message ->
        onSnackBarMessage(message)
      }
      .launchIn(scope)
  }
}