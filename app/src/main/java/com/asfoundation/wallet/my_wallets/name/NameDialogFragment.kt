package com.asfoundation.wallet.my_wallets.name

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
import com.asf.wallet.databinding.FragmentMyWalletsNameBinding
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NameDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<NameDialogState, NameDialogSideEffect> {

  @Inject
  lateinit var navigator: NameDialogNavigator

  private val viewModel: NameDialogViewModel by viewModels()
  private val views by viewBinding(FragmentMyWalletsNameBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = FragmentMyWalletsNameBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    views.nameInput.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

      override fun afterTextChanged(s: Editable?) = (s.isNullOrEmpty().not()).let {
        views.save.isEnabled = it
      }
    })
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogThemeDraggable

  override fun onStateChanged(state: NameDialogState) {
    when (val asyncValue = state.walletNameAsync) {
      Async.Uninitialized,
      is Async.Loading -> showWalletInfoLoading()
      is Async.Fail -> Unit
      is Async.Success -> showWalletInfo(asyncValue())
    }
  }

  override fun onSideEffect(sideEffect: NameDialogSideEffect) {
    when (sideEffect) {
      NameDialogSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  private fun showWalletInfoLoading() {
    views.loading.visibility = View.VISIBLE
    views.title.visibility = View.GONE
    views.nameInput.visibility = View.GONE
    views.save.visibility = View.GONE
    views.cancel.visibility = View.GONE
  }

  private fun showWalletInfo(name: String) {
    views.loading.visibility = View.GONE
    views.title.visibility = View.VISIBLE
    views.nameInput.visibility = View.VISIBLE
    views.save.visibility = View.VISIBLE
    views.cancel.visibility = View.VISIBLE
    views.nameInput.setText(name)
    views.save.setOnClickListener { viewModel.setName(views.nameInput.getText()) }
    views.cancel.setOnClickListener { navigator.navigateBack() }
  }

  companion object {
    internal const val WALLET_ADDRESS_KEY = "wallet_address"
    internal const val WALLET_NAME_KEY = "wallet_name"
  }
}