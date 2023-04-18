package com.asfoundation.wallet.my_wallets.balance_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentMyWalletsBalanceDetailsBinding
import com.appcoins.wallet.core.utils.android_common.extensions.getDrawableURI
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BalanceDetailsFragment : BottomSheetDialogFragment() {

  @Inject
  lateinit var navigator: BalanceDetailsNavigator

  private val views by viewBinding(FragmentMyWalletsBalanceDetailsBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = FragmentMyWalletsBalanceDetailsBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setData()
    setListeners()
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogThemeDraggable

  private fun setData() = requireArguments().run {
    views.totalBalanceTextView.text = getString(FIAT_BALANCE_KEY)
    views.appcValue.text = getString(APPC_BALANCE_KEY)
    views.appccValue.text = getString(CREDITS_BALANCE_KEY)
    views.ethValue.text = getString(ETHEREUM_BALANCE_KEY)
  }

  private fun setListeners() {
    views.appcFrame.setOnClickListener {
      navigator.navigateToTokenInfo(
        "${getString(R.string.appc_token_name)} (${getString(R.string.p2p_send_currency_appc)})",
        requireContext().getDrawableURI(R.drawable.ic_appc),
        getString(R.string.balance_appcoins_body),
        false
      )
    }
    views.appccFrame.setOnClickListener {
      navigator.navigateToTokenInfo(
        "${getString(R.string.appc_credits_token_name)} (${getString(R.string.p2p_send_currency_appc_c)})",
        requireContext().getDrawableURI(R.drawable.ic_appc_c_token),
        getString(R.string.balance_appccreditos_body),
        true
      )
    }
    views.ethFrame.setOnClickListener {
      navigator.navigateToTokenInfo(
        "${getString(R.string.ethereum_token_name)} (${getString(R.string.p2p_send_currency_eth)})",
        requireContext().getDrawableURI(R.drawable.ic_eth_token),
        getString(R.string.balance_ethereum_body),
        false
      )
    }
  }

  companion object {
    internal const val FIAT_BALANCE_KEY = "fiat_balance"
    internal const val APPC_BALANCE_KEY = "appc_balance"
    internal const val CREDITS_BALANCE_KEY = "credits_balance"
    internal const val ETHEREUM_BALANCE_KEY = "ethereum_balance"
  }
}