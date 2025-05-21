package com.asfoundation.wallet.home.bottom_sheet


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.asf.wallet.R
import com.asf.wallet.databinding.HomeManageWalletBottomSheetLayoutBinding
import com.asfoundation.wallet.home.bottom_sheet.HomeDetailsBalanceBottomSheetFragment.Companion.BALANCE_VALUE
import com.asfoundation.wallet.ui.webview_login.WebViewLoginActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeManageWalletBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<HomeManageWalletBottomSheetState, HomeManageWalletBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: HomeManageWalletBottomSheetNavigator

  private val viewModel: HomeManageWalletBottomSheetViewModel by viewModels()
  private val views by viewBinding(HomeManageWalletBottomSheetLayoutBinding::bind)

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  companion object {
    const val CAN_TRANSFER = "can_transfer"
    @JvmStatic
    fun newInstance(): HomeManageWalletBottomSheetFragment {
      return HomeManageWalletBottomSheetFragment()
    }
  }


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = HomeManageWalletBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val canTransfer = arguments?.getBoolean(CAN_TRANSFER)
    views.transferWalletView.isGone = canTransfer != true
    setListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeDraggable
  }

  private fun setListeners() {
    views.signInWalletView.setOnClickListener {
      buttonsAnalytics.sendDefaultButtonClickAnalytics(fragmentName, getString(R.string.home_sign_in_button))
      this.dismiss()
//      navigator.navigateToManageWallet(navController())
      //TODO:
      val url =
        "https://wallet.dev.aptoide.com/pt_PT/wallet/sign-in?domain=com.appcoins.wallet.dev&payment_channel=wallet_app"
      val intent = Intent(requireContext(), WebViewLoginActivity::class.java)
      intent.putExtra(WebViewLoginActivity.URL, url)
      startActivity(intent)  //openLoginLauncher.launch(intent)
    }

    views.backupWalletView.setOnClickListener {
      buttonsAnalytics.sendDefaultButtonClickAnalytics(fragmentName, getString(R.string.my_wallets_action_backup_wallet))
      viewModel.onBackupClick()
    }
    views.manageWalletView.setOnClickListener {
      buttonsAnalytics.sendDefaultButtonClickAnalytics(fragmentName, getString(R.string.manage_wallet_button))
      this.dismiss()
      navigator.navigateToManageWallet(navController())
    }

    views.recoverWalletView.setOnClickListener {
      buttonsAnalytics.sendDefaultButtonClickAnalytics(fragmentName, getString(R.string.my_wallets_action_recover_wallet))
      this.dismiss()
      navigator.navigateToRecoverWallet()
    }

    views.transferWalletView.setOnClickListener {
      buttonsAnalytics.sendDefaultButtonClickAnalytics(fragmentName, getString(R.string.home_transfer_balance_button))
      this.dismiss()
      navigator.navigateToTransfer(navController())
    }
  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }

  override fun onSideEffect(sideEffect: HomeManageWalletBottomSheetSideEffect) {
    when (sideEffect) {
      is HomeManageWalletBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  override fun onStateChanged(state: HomeManageWalletBottomSheetState) {
    when (state.currentWalletAsync) {
      is Async.Success -> {
        this.dismiss()
        viewModel.sendOpenBackupEvent()
        with(state.currentWalletAsync.value!!) {
          navigator.navigateToBackup(navController(), wallet, name)
        }
      }

      else -> {}
    }
  }


}