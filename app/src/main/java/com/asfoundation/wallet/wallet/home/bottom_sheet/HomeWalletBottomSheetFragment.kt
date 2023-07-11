package com.asfoundation.wallet.wallet.home.bottom_sheet


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asf.wallet.databinding.HomeManageWalletBottomSheetLayoutBinding
import com.asfoundation.wallet.manage_wallets.bottom_sheet.HomeManageWalletBottomSheetNavigator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeWalletBottomSheetFragment() : BottomSheetDialogFragment(),
  SingleStateFragment<HomeManageWalletBottomSheetState, HomeManageWalletBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: HomeManageWalletBottomSheetNavigator

  private val viewModel: HomeManageWalletBottomSheetViewModel by viewModels()
  private val views by viewBinding(HomeManageWalletBottomSheetLayoutBinding::bind)

  companion object {
    @JvmStatic
    fun newInstance(): HomeWalletBottomSheetFragment {
      return HomeWalletBottomSheetFragment()
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
     /*views.newWalletIcon.setOnClickListener {
       //open navigation
     }
     views.newWalletIcon.setOnClickListener {
       //open navigation
     }*/

   }


   override fun onSideEffect(sideEffect: HomeManageWalletBottomSheetSideEffect) {
     when (sideEffect) {
       is HomeManageWalletBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
     }
   }

  override fun onStateChanged(state: HomeManageWalletBottomSheetState) {
  }


}