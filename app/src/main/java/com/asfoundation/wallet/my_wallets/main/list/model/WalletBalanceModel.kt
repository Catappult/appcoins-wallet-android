package com.asfoundation.wallet.my_wallets.main.list.model

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.my_wallets.main.list.WalletsListEvent
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.google.android.material.card.MaterialCardView
import java.math.BigDecimal

@EpoxyModelClass
abstract class WalletBalanceModel : EpoxyModelWithHolder<WalletBalanceModel.WalletBalanceHolder>() {

  @EpoxyAttribute
  lateinit var walletBalance: WalletBalance

  @EpoxyAttribute
  lateinit var balanceAsync: Async<BalanceScreenModel>

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  lateinit var formatter: CurrencyFormatUtils

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var walletClickListener: ((WalletsListEvent) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_wallet_balance

  override fun bind(holder: WalletBalanceHolder) {
    holder.setListeners()
    when (val balance = balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        holder.appccValueSkeleton.playAnimation()
        holder.appccValueSkeleton.visibility = View.VISIBLE
        holder.appcValueSkeleton.playAnimation()
        holder.appcValueSkeleton.visibility = View.VISIBLE
        holder.ethValueSkeleton.playAnimation()
        holder.ethValueSkeleton.visibility = View.VISIBLE
        holder.totalBalanceSkeleton.playAnimation()
        holder.totalBalanceSkeleton.visibility = View.VISIBLE
      }
      is Async.Fail -> Unit
      is Async.Success -> {
        val balanceScreenModel = balance()
        holder.updateTokenValue(balanceScreenModel.appcBalance, WalletCurrency.APPCOINS)
        holder.updateTokenValue(balanceScreenModel.creditsBalance, WalletCurrency.CREDITS)
        holder.updateTokenValue(balanceScreenModel.ethBalance, WalletCurrency.ETHEREUM)
        holder.updateOverallBalance(balanceScreenModel.overallFiat)
      }
    }
  }

  private fun WalletBalanceHolder.setListeners() {
    appcoinsCardView.setOnClickListener {
      walletClickListener?.invoke(
          WalletsListEvent.TokenClick(WalletsListEvent.TokenClick.Token.APPC))
    }
    appcoinsCreditsCardView.setOnClickListener {
      walletClickListener?.invoke(
          WalletsListEvent.TokenClick(WalletsListEvent.TokenClick.Token.APPC_C))
    }
    ethCardView.setOnClickListener {
      walletClickListener?.invoke(
          WalletsListEvent.TokenClick(WalletsListEvent.TokenClick.Token.ETH))
    }
  }

  private fun WalletBalanceHolder.updateTokenValue(balance: TokenBalance,
                                                   tokenCurrency: WalletCurrency) {
    val tokenBalance = getTokenValueText(balance, tokenCurrency)
    if (tokenBalance != "-1") {
      when (tokenCurrency) {
        WalletCurrency.CREDITS -> {
          appccValueSkeleton.visibility = View.GONE
          appccValueSkeleton.cancelAnimation()
          appccValue.text = tokenBalance
          appccValue.visibility = View.VISIBLE
        }
        WalletCurrency.APPCOINS -> {
          appcValueSkeleton.visibility = View.GONE
          appcValueSkeleton.cancelAnimation()
          appcValue.text = tokenBalance
          appcValue.visibility = View.VISIBLE
        }
        WalletCurrency.ETHEREUM -> {
          ethValueSkeleton.visibility = View.GONE
          ethValueSkeleton.cancelAnimation()
          ethValue.text = tokenBalance
          ethValue.visibility = View.VISIBLE
        }
        else -> return
      }
    }
  }

  private fun getFiatBalanceText(balance: FiatValue): String {
    var overallBalance = "-1"
    if (balance.amount.compareTo(BigDecimal("-1")) == 1) {
      overallBalance = formatter.formatCurrency(balance.amount)
    }
    if (overallBalance != "-1") {
      return balance.symbol + overallBalance
    }
    return overallBalance
  }

  private fun getTokenValueText(balance: TokenBalance, tokenCurrency: WalletCurrency): String {
    var tokenBalance = "-1"
    val fiatCurrency = balance.fiat.symbol
    if (balance.token.amount.compareTo(BigDecimal("-1")) == 1) {
      tokenBalance = formatter.formatCurrency(balance.token.amount, tokenCurrency)
    }
    return tokenBalance
  }

  private fun WalletBalanceHolder.updateOverallBalance(balance: FiatValue) {
    val overallBalance = getFiatBalanceText(balance)
    if (overallBalance != "-1") {
      totalBalanceSkeleton.visibility = View.GONE
      totalBalanceSkeleton.cancelAnimation()
      totalBalanceTextView.text = overallBalance
      totalBalanceTextView.visibility = View.VISIBLE
    }
  }

  class WalletBalanceHolder : BaseViewHolder() {
    val totalBalanceTextView by bind<TextView>(R.id.total_balance_text_view)
    val totalBalanceSkeleton by bind<LottieAnimationView>(R.id.total_balance_skeleton)

    val appcoinsCardView by bind<MaterialCardView>(R.id.appcoins_card_view)
    val appcValue by bind<TextView>(R.id.appc_value)
    val appcValueSkeleton by bind<LottieAnimationView>(R.id.appc_value_skeleton)

    val appcoinsCreditsCardView by bind<MaterialCardView>(R.id.appcoins_credits_card_view)
    val appccValue by bind<TextView>(R.id.appcc_value)
    val appccValueSkeleton by bind<LottieAnimationView>(R.id.appcc_value_skeleton)

    val ethCardView by bind<MaterialCardView>(R.id.eth_card_view)
    val ethValue by bind<TextView>(R.id.eth_value)
    val ethValueSkeleton by bind<LottieAnimationView>(R.id.eth_value_skeleton)
  }
}

