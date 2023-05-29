package com.appcoins.wallet.core.arch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface SingleStateFragment<S : ViewState, E : SideEffect> {

  fun onStateChanged(state: S)

  fun onSideEffect(sideEffect: E)

  fun BaseViewModel<S, E>.collectStateAndEvents(lifecycle: Lifecycle,
                                                scope: LifecycleCoroutineScope) {
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
  }

  fun NewBaseViewModel<S>.collectStateAndEvents(lifecycle: Lifecycle,
                                                scope: LifecycleCoroutineScope) {
    stateFlow
      .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
      .onEach { state ->
        onStateChanged(state)
      }
      .launchIn(scope)
  }
}