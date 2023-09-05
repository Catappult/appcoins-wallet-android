package com.asfoundation.wallet.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.feature.backup.data.result.FailedBackup
import com.appcoins.wallet.feature.backup.data.result.SuccessfulBackup
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsRoute
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsSideEffect
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsState
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsViewModel
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSaveOptionsComposeFragment :
  BasePageViewFragment(),
  SingleStateFragment<BackupSaveOptionsState, BackupSaveOptionsSideEffect>,
  Navigator {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  @Inject
  lateinit var navigator: BackupSaveOptionsNavigator

  companion object {
    fun newInstance() = BackupSaveOptionsComposeFragment()
    const val PASSWORD_KEY = "password"
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val SAVE_PLACE_KEY = "save_backup_place"
  }
  private val viewModel: BackupSaveOptionsViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.walletAddress = requireArguments().getString(WALLET_ADDRESS_KEY, "") ?: ""
    viewModel.password = requireArguments().getString(PASSWORD_KEY, "") ?: ""
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: BackupSaveOptionsState) {
    when (state.saveOptionAsync) {
      Async.Uninitialized -> {
        // Empty block
      }
      is Async.Loading -> {
        // Empty block
      }
      is Async.Fail -> {
        navigator.showErrorScreen()
      }
      is Async.Success -> {
        when (state.saveOptionAsync.value) {
          is SuccessfulBackup -> {
            navigator.showWalletSuccessScreen()
          }
          is FailedBackup -> {
            navigator.showErrorScreen()
          }
          else -> {}
        }
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        WalletTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            BackupSaveOptionsRoute(
              onChatClick = { displayChat() },
              onSaveOnDevice = {
                navigator.showSaveOnDeviceFragment(viewModel.walletAddress, viewModel.password)
              })
          }
        }
      }
    }
  }

  override fun onSideEffect(sideEffect: BackupSaveOptionsSideEffect) {
    when (sideEffect) {
      is BackupSaveOptionsSideEffect.NavigateToSuccess -> {
        viewModel.showLoading(false)
        navigator.showWalletSuccessScreen()
      }

      BackupSaveOptionsSideEffect.ShowError -> {
        viewModel.showLoading(false)
        navigator.showErrorScreen()
      }

      else -> {}
    }
  }
}
