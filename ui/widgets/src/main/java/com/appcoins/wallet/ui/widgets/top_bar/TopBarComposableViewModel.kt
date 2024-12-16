package com.appcoins.wallet.ui.widgets.top_bar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.ui.widgets.top_bar.use_case.GetBadgeVisibleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TopBarComposableViewModel @Inject constructor(
  getBadgeVisibleUseCase: GetBadgeVisibleUseCase,
) : ViewModel() {

  private val viewModelState = MutableStateFlow(false)

  val uiState = viewModelState
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelState.value
    )

  init {
    getBadgeVisibleUseCase()
      .onEach { isVisible -> viewModelState.update { isVisible } }
      .launchIn(viewModelScope)
  }
}
