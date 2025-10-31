package com.appcoins.wallet.core.arch

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModelWithSnackBar<S : ViewState, E : SideEffect>(initialState: S) :
  BaseViewModel<S, E>(initialState) {
  private val _snackBarMessagesFlow = Channel<SnackBarMessage>(Channel.BUFFERED)
  val snackBarMessagesFlow = _snackBarMessagesFlow.receiveAsFlow()

  protected fun sendSnackBarMessage(message: SnackBarMessage) {
    _snackBarMessagesFlow.trySend(message)
  }
}