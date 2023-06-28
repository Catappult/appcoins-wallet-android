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
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSaveOptionsComposeFragment : BasePageViewFragment(), SingleStateFragment<BackupSaveOptionsState, BackupSaveOptionsSideEffect> {
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
       // showErrorMessage(FailedRedeem.GenericError(""))
      }
      is Async.Success -> {
        state.saveOptionAsync.value?.let { successRequest ->
          if (successRequest)
            navigateToBackupWalletSuccess(navController())
         // else
            //showErrorMessage(redeemState as? FailedRedeem ?: FailedRedeem.GenericError(""))
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

  override fun onSideEffect(sideEffect: BackupSaveOptionsSideEffect) {
    TODO("Not yet implemented")
  }


}

