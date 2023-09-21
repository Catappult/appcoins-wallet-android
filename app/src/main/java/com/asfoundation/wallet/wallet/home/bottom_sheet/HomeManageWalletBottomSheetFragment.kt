package com.asfoundation.wallet.wallet.home.bottom_sheet


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.asf.wallet.R
import com.asf.wallet.databinding.HomeManageWalletBottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeManageWalletBottomSheetFragment() : BottomSheetDialogFragment(),
  SingleStateFragment<HomeManageWalletBottomSheetState, HomeManageWalletBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: HomeManageWalletBottomSheetNavigator

  private val viewModel: HomeManageWalletBottomSheetViewModel by viewModels()
  private val views by viewBinding(HomeManageWalletBottomSheetLayoutBinding::bind)

  companion object {
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
    views.backupWalletView.setOnClickListener {
      viewModel.onBackupClick()
    }
    views.manageWalletView.setOnClickListener {
      this.dismiss()
      navigator.navigateToManageWallet(navController())
    }
    views.recoverWalletView.setOnClickListener {
      this.dismiss()
      navigator.navigateToRecoverWallet()
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