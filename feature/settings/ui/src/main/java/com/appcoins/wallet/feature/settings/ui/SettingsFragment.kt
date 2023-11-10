package com.appcoins.wallet.feature.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.appcoins.wallet.feature.settings.ui.SettingsViewModel.UiState
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.TopBar
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BasePageViewFragment() {

  private val viewModel: SettingsViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { SettingsView() } }
  }

  @Composable
  fun SettingsView() {
    Scaffold(
      topBar = {
        Surface {
          TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() })
        }
      },
      containerColor = WalletColors.styleguide_blue
    ) { padding ->
      when (viewModel.uiState.collectAsState().value) {
        is UiState.Success -> {
          SettingsScreen(padding)
        }

        UiState.Loading -> {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator()
          }
        }

        else -> {}
      }
    }
  }

  @Composable
  fun SettingsScreen(paddingValues: PaddingValues) {
    Column(modifier = Modifier.padding(paddingValues)) {

    }
  }
}
