package com.asfoundation.wallet.manage_wallets.bottom_sheet


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.ui.widgets.WalletTextFieldView
import com.asf.wallet.databinding.ManageWalletNameBottomSheetLayoutBinding
import com.asfoundation.wallet.wallet_reward.RewardSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageWalletNameBottomSheetFragment() : BottomSheetDialogFragment(),
  SingleStateFragment<ManageWalletNameBottomSheetState, ManageWalletNameBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: ManageWalletNameBottomSheetNavigator

  private val viewModel: ManageWalletNameBottomSheetViewModel by viewModels()
  private val views by viewBinding(ManageWalletNameBottomSheetLayoutBinding::bind)

  private val manageWalletSharedViewModel: ManageWalletSharedViewModel by activityViewModels()

  companion object {
    const val WALLET_NAME = "wallet_name"
    const val WALLET_ADDRESS = "wallet_address"

    @JvmStatic
    fun newInstance(): ManageWalletNameBottomSheetFragment {
      return ManageWalletNameBottomSheetFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = ManageWalletNameBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    views.textWalletNameBottomSheetString.setType(WalletTextFieldView.Type.FILLED)
    views.textWalletNameBottomSheetString.setColor(
      ContextCompat.getColor(
        requireContext(),
        R.color.styleguide_blue_secondary
      )
    )

    val walletAddress = arguments?.getString(WALLET_ADDRESS)
    val walletName = arguments?.getString(WALLET_NAME)
    if (!walletName.isNullOrEmpty()) {
      views.textWalletNameBottomSheetString.setText(walletName)
    }
    setListeners(walletAddress)
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

  private fun setListeners(walletAddress: String?) {
    views.manageWalletBottomSheetSubmitButton.setOnClickListener {
      if (walletAddress.isNullOrEmpty()) {
        viewModel.createWallet(views.textWalletNameBottomSheetString.getText().trim())
      } else {
        showLoading()
        viewModel.setWalletName(
          walletAddress,
          views.textWalletNameBottomSheetString.getText().trim()
        )
      }
    }

    views.textWalletNameBottomSheetString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.manageWalletBottomSheetSubmitButton.isEnabled = s.isNotEmpty()
      }

      override fun afterTextChanged(s: Editable) = Unit
    })
  }

  override fun onStateChanged(state: ManageWalletNameBottomSheetState) {
    when (val clickAsync = state.walletNameAsync) {
      is Async.Loading -> {
        if (clickAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
        navigator.navigateBack()
      }
      is Async.Success -> {
        manageWalletSharedViewModel.onBottomSheetDismissed()
        navigator.navigateBack()
      }
      Async.Uninitialized -> {}
    }
  }

  override fun onSideEffect(sideEffect: ManageWalletNameBottomSheetSideEffect) {
    when (sideEffect) {
      is ManageWalletNameBottomSheetSideEffect.NavigateBack -> {
        manageWalletSharedViewModel.onBottomSheetDismissed()
        navigator.navigateBack()
      }
    }
  }


  private fun showLoading() {
    hideAll()
    views.manageWalletBottomSheetSystemView.visibility = View.VISIBLE
    views.manageWalletBottomSheetSystemView.showProgress(true)
  }

  private fun hideAll() {
    views.textWalletNameBottomSheetString.visibility = View.GONE
    views.manageWalletBottomSheetTitle.visibility = View.GONE
    views.manageWalletBottomSheetSubmitButton.visibility = View.GONE
  }


}