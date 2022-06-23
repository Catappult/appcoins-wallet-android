package com.asfoundation.wallet.my_wallets.more

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentMyWalletsMoreBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.util.convertDpToPx
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<MoreDialogState, MoreDialogSideEffect> {

  @Inject
  lateinit var navigator: MoreDialogNavigator

  private val viewModel: MoreDialogViewModel by viewModels()
  private val views by viewBinding(FragmentMyWalletsMoreBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = inflater.inflate(R.layout.fragment_my_wallets_more, container, false)

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

  override fun onResume() {
    super.onResume()
    viewModel.refreshData()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

  override fun onStateChanged(state: MoreDialogState) {
    val wallets = state.walletsAsync()
    views.deleteWalletCardView.visibility =
      if (wallets?.isEmpty() == false) View.VISIBLE else View.GONE
    views.walletsView.apply {
      removeAllViews()
      wallets?.forEach { wallet ->
        addView(
          buildWalletView(
            wallet.isSelected,
            wallet.walletName,
            wallet.fiatBalance,
            if (wallet.isSelected) null else ({
              viewModel.changeActiveWallet(wallet.walletAddress)
            }),
          )
        )
      }
    }
  }

  override fun onSideEffect(sideEffect: MoreDialogSideEffect) {
    when (sideEffect) {
      MoreDialogSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  private fun setListeners() {
    views.newWalletCardView.setOnClickListener { navigator.navigateToCreateNewWallet() }
    views.recoverWalletCardView.setOnClickListener { navigator.navigateToRestoreWallet() }
    views.deleteWalletCardView.setOnClickListener {
      navigator.navigateToRemoveWallet(
        viewModel.state.walletAddress,
        viewModel.state.totalFiatBalance,
        viewModel.state.appcoinsBalance,
        viewModel.state.creditsBalance,
        viewModel.state.ethereumBalance
      )
    }
  }

  private fun buildWalletView(
    isSelected: Boolean,
    name: String,
    balance: String,
    onClick: ((View) -> Unit)? = null
  ): View = MaterialCardView(context).apply {
    layoutParams = MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
      .apply {
        topMargin = 8.dp
      }
    elevation = 0F
    radius = 14.dp.toFloat()
    rippleColor = resources.getColorStateList(R.color.grey_8B, null)
    onClick?.let { setOnClickListener(onClick) }
    val bColor = if (isSelected) resources.getColor(R.color.bottom_nav_top_up_blue, null)
    else Color.parseColor("#F2F2F2")
    addView(
      LinearLayoutCompat(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 40.dp)
        orientation = LinearLayoutCompat.HORIZONTAL
        setBackgroundColor(bColor)
        gravity = Gravity.CENTER_VERTICAL
        updatePadding(left = 13.dp, right = 32.dp)
        addView(
          ImageView(context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(15.dp, LayoutParams.WRAP_CONTENT)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.ic_check_mark)
            visibility = if (isSelected) VISIBLE else INVISIBLE
          }
        )
        val tColor = resources.getColor(
          if (isSelected) R.color.white else R.color.bottom_nav_top_up_blue,
          null
        )
        addView(
          TextView(context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1F)
              .apply {
                marginStart = 12.dp
                marginEnd = 24.dp
              }
            setTextColor(tColor)
            typeface = Typeface.create("sans-serif-medium", STYLE_NORMAL)
            textSize = 14.toFloat()
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
            text = name
          }
        )
        addView(
          TextView(context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
              LayoutParams.WRAP_CONTENT,
              LayoutParams.WRAP_CONTENT
            )
            setTextColor(tColor)
            typeface = Typeface.create("sans-serif-medium", STYLE_NORMAL)
            textSize = 14.toFloat()
            maxLines = 1
            text = balance
          }
        )
      }
    )
  }

  private val Int.dp get() = this.convertDpToPx(resources)

  companion object {
    internal const val WALLET_ADDRESS_KEY = "wallet_address"
    internal const val FIAT_BALANCE_KEY = "fiat_balance"
    internal const val APPC_BALANCE_KEY = "appc_balance"
    internal const val CREDITS_BALANCE_KEY = "credits_balance"
    internal const val ETHEREUM_BALANCE_KEY = "ethereum_balance"
  }
}