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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsRoute
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsViewModel
import com.appcoins.wallet.feature.changecurrency.ui.ChangeFiatCurrencyRoute
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSaveOptionsComposeFragment : BasePageViewFragment() {
  @Inject
  lateinit var displayChat: DisplayChatUseCase

  companion object {
    fun newInstance() = BackupSaveOptionsComposeFragment()
      const val PASSWORD_KEY = "password"
      const val WALLET_ADDRESS_KEY = "wallet_address"

  }
  private val viewModel: BackupSaveOptionsViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.walletAddress = requireArguments().getString(WALLET_ADDRESS_KEY, PASSWORD_KEY) ?: ""
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        WalletTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            BackupSaveOptionsRoute(
              onExitClick = { handleBackPress() },
              onChatClick = { displayChat() },
              onSendEmailClick = {navigateToBackupWalletSuccess(navController())}
            )
          }
        }
      }
    }
  }

  private fun handleBackPress() {
    navController().popBackStack()
  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }

  private fun navigateToBackupWalletSuccess(
    mainNavController: NavController
  ) {
    mainNavController.navigate(R.id.backup_wallet_success_screen)
  }



}

