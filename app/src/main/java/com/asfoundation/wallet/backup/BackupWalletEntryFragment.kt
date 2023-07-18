package com.asfoundation.wallet.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryData
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryFragment
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryRoute
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryViewModel
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsViewModel
import com.appcoins.wallet.feature.changecurrency.ui.ChangeFiatCurrencyRoute
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.appcoins.wallet.core.arch.data.Navigator
import com.asfoundation.wallet.ui.settings.entry.SettingsInteractor
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers

@AndroidEntryPoint
class BackupWalletEntryFragment  : BasePageViewFragment(), Navigator{

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  @Inject
  lateinit var navigator : BackupEntryNavigator


  @Inject
  lateinit var settingsInteractor : SettingsInteractor


  val networkScheduler= Schedulers.io()
  val viewScheduler = AndroidSchedulers.mainThread()
  val disposables = CompositeDisposable()



  companion object {
    fun newInstance() = BackupWalletEntryFragment()
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val WALLET_NAME = "wallet_name"
    const val PASSWORD_KEY = "password"
    const val WALLET_MODEL_KEY = "wallet_model"
  }


  private val viewModel: BackupEntryViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.walletAddress = requireArguments().getString(WALLET_ADDRESS_KEY) ?: "" // aq
    viewModel.walletName = requireArguments().getString(WALLET_NAME) ?: ""
    viewModel.showBalance(viewModel.walletAddress)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        WalletTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            BackupEntryRoute(
              onExitClick = { handleBackPress() },
              onChatClick = { displayChat() },
              onChooseWallet = { onBackupPreferenceClick() },
              onNextClick = {navigateToBackupWalletEntry(viewModel.walletAddress, navController(), viewModel.password)}
            )
          }
        }
      }
    }
  }


  private fun handleBackPress() {
    parentFragmentManager.popBackStack()
  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }

  private fun navigateToBackupWalletEntry(
    walletAddress: String,
    mainNavController: NavController,
    password: String

  ) {
    val bundle = Bundle()
    bundle.putString(WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(PASSWORD_KEY, password)
    mainNavController.navigate(R.id.action_backup_entry_to_screen_options, args = bundle)
  }

  fun onBackupPreferenceClick() {
    disposables.add(settingsInteractor.retrieveWallets()
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSuccess { navigator.showWalletChooseScreen(
        walletModel = it,
        mainNavController = navController(),
      )}
      .subscribe({}, {  })
    )
  }



}
