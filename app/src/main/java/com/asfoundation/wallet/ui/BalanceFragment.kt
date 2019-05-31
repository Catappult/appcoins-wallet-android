package com.asfoundation.wallet.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.FiatValue
import com.google.android.material.appbar.AppBarLayout
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.balance_token_item.view.*
import kotlinx.android.synthetic.main.fragment_balance.*

class BalanceFragment : DaggerFragment(), BalanceFragmentView {

  private var activityView: BalanceActivityView? = null
  private lateinit var presenter: BalanceFragmentPresenter

  companion object {
    @JvmStatic
    fun newInstance(): BalanceFragment {
      return BalanceFragment()
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is BalanceActivityView) {
      throw IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity")
    }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BalanceFragmentPresenter(this, Schedulers.io(), AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_balance, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activityView?.setupToolbar()
    presenter.present()

    (app_bar as AppBarLayout).addOnOffsetChangedListener(
        AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
          val percentage = Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
          balance_label.alpha = 1 - percentage * 1.20f
          balance_value.alpha = 1 - percentage * 1.20f
        })
  }

  override fun setupUI() {
    (balance_value_placeholder as LottieAnimationView).playAnimation()

    appcoins_credits_token.token_icon.setImageResource(R.drawable.ic_appc_c_token)
    appcoins_credits_token.token_name.text = "AppCoins Credits"
    (appcoins_credits_token.token_balance_placeholder as LottieAnimationView).playAnimation()

    appcoins_token.token_icon.setImageResource(R.drawable.ic_appc_token)
    appcoins_token.token_name.text = "AppCoins"
    (appcoins_token.token_balance_placeholder as LottieAnimationView).playAnimation()

    ether_token.token_icon.setImageResource(R.drawable.ic_eth_token)
    ether_token.token_name.text = "Ethereum"
    (ether_token.token_balance_placeholder as LottieAnimationView).playAnimation()
  }

  override fun updateTokenValue(tokenBalance: Balance) {
    when (tokenBalance.token.currency) {
      "AppCoins Credits" -> {
        appcoins_credits_token.token_balance_placeholder.visibility = View.GONE
        (appcoins_credits_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
        appcoins_credits_token.token_balance.text = tokenBalance.token.symbol + tokenBalance.token.amount.toPlainString()
        appcoins_credits_token.token_balance.visibility = View.VISIBLE
        appcoins_credits_token.token_balance_converted.text = tokenBalance.fiat.symbol + tokenBalance.fiat.amount
        appcoins_credits_token.token_balance_converted.visibility = View.VISIBLE
      }
      "AppCoins" -> {
        appcoins_token.token_balance_placeholder.visibility = View.GONE
        (appcoins_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
        appcoins_token.token_balance.text = tokenBalance.token.symbol + tokenBalance.token.amount.toPlainString()
        appcoins_token.token_balance.visibility = View.VISIBLE
        appcoins_token.token_balance_converted.text = tokenBalance.fiat.symbol + tokenBalance.fiat.amount
        appcoins_token.token_balance_converted.visibility = View.VISIBLE
      }
      "Ethereum" -> {
        ether_token.token_balance_placeholder.visibility = View.GONE
        (ether_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
        ether_token.token_balance.text = tokenBalance.token.symbol + tokenBalance.token.amount.toPlainString()
        ether_token.token_balance.visibility = View.VISIBLE
        ether_token.token_balance_converted.text = tokenBalance.fiat.symbol + tokenBalance.fiat.amount
        ether_token.token_balance_converted.visibility = View.VISIBLE
      }
    }
  }

  override fun updateOverallBalance(overallBalance: FiatValue) {
    balance_value_placeholder.visibility = View.GONE
    (balance_value_placeholder as LottieAnimationView).cancelAnimation()
    balance_value.text = overallBalance.symbol + overallBalance.amount
    balance_value.visibility = View.VISIBLE
  }
}
