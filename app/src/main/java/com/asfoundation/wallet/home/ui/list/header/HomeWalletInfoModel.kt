package com.asfoundation.wallet.home.ui.list.header

import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.entity.GlobalBalance
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.google.android.material.button.MaterialButton
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
    holder.sendButton.setOnClickListener { clickListener?.invoke(HomeListClick.SendButtonClick) }
    holder.receiveButton.setOnClickListener {
      clickListener?.invoke(HomeListClick.ReceiveButtonClick)
    }
    holder.balanceClickableView.setOnClickListener {
      clickListener?.invoke(HomeListClick.BalanceClick)
    }
    holder.currencyClickableView.setOnClickListener {
      clickListener?.invoke(HomeListClick.ChangeCurrencyClick)
    }
  }

  private fun WalletInfoHolder.setWalletBalance(globalBalance: GlobalBalance) {
    val overallBalanceFiat = globalBalance.walletBalance.overallFiat
    val overallAmount = formatter.formatCurrency(overallBalanceFiat.amount, WalletCurrency.FIAT)
    if (overallBalanceFiat.amount > BigDecimal("-1") && overallBalanceFiat.symbol.isNotEmpty()) {
      balanceSkeleton.visibility = View.INVISIBLE
      balance.visibility = View.VISIBLE
      balanceSubtitle.visibility = View.VISIBLE
      currencySelector.visibility = View.VISIBLE
      balance.text = overallBalanceFiat.symbol + overallAmount
      setSubtitle(globalBalance)
    }
  }

  private fun WalletInfoHolder.setSubtitle(globalBalance: GlobalBalance) {
    val walletBalance = globalBalance.walletBalance
    val subtitle = buildCurrencyString(walletBalance.appcBalance, walletBalance.creditsBalance,
        walletBalance.ethBalance, globalBalance.showAppcoins,
        globalBalance.showCredits, globalBalance.showEthereum)
    balanceSubtitle.text = Html.fromHtml(subtitle)
  }

  private fun WalletInfoHolder.showSkeleton() {
    balance.visibility = View.INVISIBLE
    balanceSubtitle.visibility = View.INVISIBLE
    currencySelector.visibility = View.INVISIBLE
    balanceSkeleton.visibility = View.VISIBLE
    balanceSkeleton.playAnimation()
  }

  private fun buildCurrencyString(appcoinsBalance: TokenBalance, creditsBalance: TokenBalance,
                                  ethereumBalance: TokenBalance, showAppcoins: Boolean,
                                  showCredits: Boolean, showEthereum: Boolean): String {
    val stringBuilder = StringBuilder()
    val bullet = "\u00A0\u00A0\u00A0\u2022\u00A0\u00A0\u00A0"
    if (showCredits) {
      val creditsString =
          (formatter.formatCurrency(creditsBalance.token.amount, WalletCurrency.CREDITS)
              + " "
              + WalletCurrency.CREDITS.symbol)
      stringBuilder.append(creditsString)
          .append(bullet)
    }
    if (showAppcoins) {
      val appcString =
          (formatter.formatCurrency(appcoinsBalance.token.amount, WalletCurrency.APPCOINS)
              + " "
              + WalletCurrency.APPCOINS.symbol)
      stringBuilder.append(appcString)
          .append(bullet)
    }
    if (showEthereum) {
      val ethString =
          (formatter.formatCurrency(ethereumBalance.token.amount, WalletCurrency.ETHEREUM)
              + " "
              + WalletCurrency.ETHEREUM.symbol)
      stringBuilder.append(ethString)
          .append(bullet)
    }
    var subtitle = stringBuilder.toString()
    if (stringBuilder.length > bullet.length) {
      subtitle = stringBuilder.substring(0, stringBuilder.length - bullet.length)
    }
    return subtitle.replace(bullet, "<font color='#ffffff'>$bullet</font>")
  }

  class WalletInfoHolder : BaseViewHolder() {
    val balance by bind<TextView>(R.id.balance)
    val balanceSubtitle by bind<TextView>(R.id.balance_subtitle)
    val currencySelector by bind<ImageView>(R.id.currency_selector)
    val balanceSkeleton by bind<LottieAnimationView>(R.id.balance_skeleton)
    val balanceClickableView by bind<View>(R.id.balance_empty_clickable_view)
    val currencyClickableView by bind<View>(R.id.currency_empty_clickable_view)
    val sendButton by bind<MaterialButton>(R.id.send_button)
    val receiveButton by bind<MaterialButton>(R.id.receive_button)
  }
}