package com.asfoundation.wallet.my_wallets.token

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentTokenInfoBinding
import com.asfoundation.wallet.GlideApp
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TokenInfoDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<TokenInfoState, TokenInfoSideEffect> {

  @Inject
  lateinit var viewModelFactory: TokenInfoDialogViewModelFactory

  @Inject
  lateinit var navigator: TokenInfoDialogNavigator

  private val viewModel: TokenInfoDialogViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentTokenInfoBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentTokenInfoBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    views.topUpButton.setOnClickListener { navigator.navigateToTopUp() }
    views.okButton.setOnClickListener { navigator.navigateBack() }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogThemeDraggable

  override fun onStateChanged(state: TokenInfoState) {
    views.title.text = state.title
    views.description.text = state.description
    GlideApp.with(this)
        .load(state.image.toUri())
        .into(views.icon)

    views.topUpButton.visibility = if (state.showTopUp) View.VISIBLE else View.GONE
  }

  override fun onSideEffect(sideEffect: TokenInfoSideEffect) = Unit

  companion object {
    internal const val TITLE_KEY = "title"
    internal const val IMAGE_KEY = "image"
    internal const val DESCRIPTION_KEY = "description"
    internal const val SHOW_TOP_UP_KEY = "show_topup"
  }
}