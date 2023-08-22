package com.asfoundation.wallet.manage_wallets.bottom_sheet


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.asf.wallet.databinding.ManageWalletBottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageWalletBottomSheetFragment() : BottomSheetDialogFragment(),
  SingleStateFragment<ManageWalletBottomSheetState, ManageWalletBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: ManageWalletBottomSheetNavigator

  private val viewModel: ManageWalletBottomSheetViewModel by viewModels()
  private val views by viewBinding(ManageWalletBottomSheetLayoutBinding::bind)

  companion object {

    const val HAS_ONE_WALLET = "has_one_wallet"
    @JvmStatic
    fun newInstance(): ManageWalletBottomSheetFragment {
      return ManageWalletBottomSheetFragment()
    }
  }


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = ManageWalletBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setListeners()
    if (arguments?.getBoolean(HAS_ONE_WALLET) == true) {
      views.deleteWalletView.visibility = View.GONE
    }
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
    views.newWalletView.setOnClickListener {
      dismiss()
      navigator.navigateToManageNameWallet()
    }
    views.deleteWalletView.setOnClickListener {
      dismiss()
      navigator.navigateToRemoveWallet(navController())
    }
    views.recoverWalletView.setOnClickListener {
      dismiss()
      navigator.navigateToRecoverWallet()
    }

  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }

  override fun onSideEffect(sideEffect: ManageWalletBottomSheetSideEffect) {
    when (sideEffect) {
      is ManageWalletBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  override fun onStateChanged(state: ManageWalletBottomSheetState) {
  }


}