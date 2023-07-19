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
import com.appcoins.wallet.feature.backup.ui.BackupErrorRoute
import com.appcoins.wallet.feature.backup.ui.success.BackupSuccessRoute
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import javax.inject.Inject

class BackupErrorScreenFragment: BasePageViewFragment() {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  companion object {
    fun newInstance() = BackupErrorScreenFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        WalletTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            BackupErrorRoute(
              onExitClick = { handleBackPress() },
              onChatClick = { displayChat() },
            )
          }
        }
      }
    }
  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }

  private fun handleBackPress() {
    navController().popBackStack()
  }
}