package com.asfoundation.wallet.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupErrorScreenFragment : BasePageViewFragment() {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  @Inject
  lateinit var navigator: BackupEntryNavigator

  companion object {
    fun newInstance() = BackupErrorScreenFragment()
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
            BackupErrorRoute(
              onClickBack = { handleBackPress() },
              onChatClick = { displayChat() },
              onCancelBackup = { navigator.navigateToManageWallet(navController()) })
          }
        }
      }
    }
  }

  private fun navController(): NavController {
    val navHostFragment =
      requireActivity().supportFragmentManager.findFragmentById(R.id.main_host_container)
          as NavHostFragment
    return navHostFragment.navController
  }

  private fun handleBackPress() {
    parentFragmentManager.popBackStack()
  }
}
