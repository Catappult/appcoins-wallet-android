package com.asfoundation.wallet.manage_wallets.bottom_sheet


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
import com.asf.wallet.databinding.ManageWalletNameBottomSheetLayoutBinding
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

  companion object {
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
    views.manageWalletBottomSheetSubmitButton.setOnClickListener {
      viewModel.createWallet(views.textWalletNameBottomSheetString.getText().trim())
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
        navigator.navigateBack()
      }
      Async.Uninitialized -> {}
    }
  }

  override fun onSideEffect(sideEffect: ManageWalletNameBottomSheetSideEffect) {
    when (sideEffect) {
      is ManageWalletNameBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }


  private fun showLoading() {
    views.manageWalletBottomSheetSystemView.visibility = View.VISIBLE
    views.manageWalletBottomSheetSystemView.showProgress(true)
  }


}