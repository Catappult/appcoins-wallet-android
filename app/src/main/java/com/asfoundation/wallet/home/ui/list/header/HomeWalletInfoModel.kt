package com.asfoundation.wallet.home.ui.list.header

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.appcoins.wallet.ui.arch.Async
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import java.math.BigDecimal

@EpoxyModelClass
abstract class HomeWalletInfoModel : EpoxyModelWithHolder<HomeWalletInfoModel.WalletInfoHolder>() {

  @EpoxyAttribute
  lateinit var balanceAsync: Async<GlobalBalance>

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  lateinit var formatter: CurrencyFormatUtils

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((HomeListClick) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_home_wallet_info

  override fun bind(holder: WalletInfoHolder) {
    when (val balAsync = balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (balAsync() == null) {
          holder.showSkeleton()
        }
      }
      is Async.Success -> {
        holder.setWalletBalance(balAsync())
      }
      else -> Unit
    }
    holder.balanceClickableView.setOnClickListener {
      clickListener?.invoke(HomeListClick.BalanceClick)
    }
    holder.currencyClickableView.setOnClickListener {
      clickListener?.invoke(HomeListClick.ChangeCurrencyClick)
    }
  }

  @SuppressLint("SetTextI18n")
  private fun WalletInfoHolder.setWalletBalance(globalBalance: GlobalBalance) {
    val creditsBalanceFiat = globalBalance.walletBalance.creditsOnlyFiat
    val creditsBalanceFiatAmount =
      formatter.formatCurrency(creditsBalanceFiat.amount, WalletCurrency.FIAT)
    if (creditsBalanceFiat.amount > BigDecimal("-1") && creditsBalanceFiat.symbol.isNotEmpty()) {
      balanceSkeleton.visibility = View.INVISIBLE
      balance.visibility = View.VISIBLE
      balanceSubtitle.visibility = View.VISIBLE
      currencySelector.visibility = View.VISIBLE
      balance.text = creditsBalanceFiat.symbol + creditsBalanceFiatAmount
      setSubtitle(globalBalance)
    }
  }

  private fun WalletInfoHolder.setSubtitle(globalBalance: GlobalBalance) {
    val walletBalance = globalBalance.walletBalance
    val subtitle = creditsString(walletBalance.creditsBalance)
    balanceSubtitle.text = subtitle
  }

  private fun creditsString(creditsBalance: TokenBalance): String {
    return "${
      formatter.formatCurrency(
        creditsBalance.token.amount,
        WalletCurrency.CREDITS
      )
    } ${WalletCurrency.CREDITS.symbol}"
  }

  private fun WalletInfoHolder.showSkeleton() {
    balance.visibility = View.INVISIBLE
    balanceSubtitle.visibility = View.INVISIBLE
    currencySelector.visibility = View.INVISIBLE
    balanceSkeleton.visibility = View.VISIBLE
    balanceSkeleton.playAnimation()
  }

  class WalletInfoHolder : BaseViewHolder() {
    val balance by bind<TextView>(R.id.balance)
    val balanceSubtitle by bind<TextView>(R.id.balance_subtitle)
    val currencySelector by bind<ImageView>(R.id.currency_selector)
    val balanceSkeleton by bind<LottieAnimationView>(R.id.balance_skeleton)
    val balanceClickableView by bind<View>(R.id.balance_empty_clickable_view)
    val currencyClickableView by bind<View>(R.id.currency_empty_clickable_view)
  }
}