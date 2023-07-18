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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.feature.backup.ui.BackupErrorScreen
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsRoute
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsSideEffect
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsState
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsViewModel
import com.appcoins.wallet.feature.changecurrency.ui.ChangeFiatCurrencyRoute
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.redeem_gift.bottom_sheet.RedeemGiftBottomSheetState
import com.asfoundation.wallet.redeem_gift.repository.FailedRedeem
import com.asfoundation.wallet.redeem_gift.repository.SuccessfulRedeem
import com.asfoundation.wallet.wallet_reward.RewardFragmentDirections
import com.asfoundation.wallet.wallet_reward.RewardNavigator
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSaveOptionsComposeFragment : BasePageViewFragment(), SingleStateFragment<BackupSaveOptionsState, BackupSaveOptionsSideEffect>,
Navigator{

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  @Inject
  lateinit var navigator: BackupSaveOptionsNavigator

  companion object {
    fun newInstance() = BackupSaveOptionsComposeFragment()
      const val PASSWORD_KEY = "password"
      const val WALLET_ADDRESS_KEY = "wallet_address"

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
      is Async.Uninitialized ->  {
      }
      is Async.Loading -> {

      }
      is Async.Fail -> {
       navigator.showErrorScreen()
      }
      is Async.Success -> {
        state.saveOptionAsync.value?.let { successRequest ->
          if (successRequest)
            navigator.showWalletSuccessScreen()
       }
      }
    }
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
              onExitClick = { navigator.handleBackPress() },
              onChatClick = { displayChat() },
              onSendEmailClick = {navigator.showWalletSuccessScreen()},
              onSaveOnDevice = { navigator.showSaveOnDeviceFragment(viewModel.walletAddress, viewModel.password, navController()) }
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

  override fun onSideEffect(sideEffect: BackupSaveOptionsSideEffect) {
    TODO("Not yet implemented")
  }


}

