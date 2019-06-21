package com.asfoundation.wallet.ui.balance

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.APPC_CURRENCY
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.APPC_C_CURRENCY
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.ETH_CURRENCY
import com.asfoundation.wallet.ui.iab.FiatValue
import com.google.android.material.appbar.AppBarLayout
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.balance_token_item.view.*
import kotlinx.android.synthetic.main.fragment_balance.*
import java.math.BigDecimal
import javax.inject.Inject

class BalanceFragment : DaggerFragment(), BalanceFragmentView {

  @Inject
  lateinit var balanceInteract: BalanceInteract

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
    presenter = BalanceFragmentPresenter(this, balanceInteract,
        Schedulers.io(),
        AndroidSchedulers.mainThread(), CompositeDisposable())
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
          setAlpha(balance_label, percentage)
          setAlpha(balance_value, percentage)
          setAlpha(balance_label_placeholder, percentage)
          setAlpha(balance_value_placeholder, percentage)
        })
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun setupUI() {
    balance_value_placeholder.playAnimation()
    balance_label_placeholder.playAnimation()

    appcoins_credits_token.token_icon.setImageResource(R.drawable.ic_appc_c_token)
    appcoins_credits_token.token_name.text = getString(R.string.appc_credits_token_name)
    (appcoins_credits_token.token_balance_placeholder as LottieAnimationView).playAnimation()

    appcoins_token.token_icon.setImageResource(R.drawable.ic_appc_token)
    appcoins_token.token_name.text = getString(R.string.appc_token_name)
    (appcoins_token.token_balance_placeholder as LottieAnimationView).playAnimation()

    ether_token.token_icon.setImageResource(R.drawable.ic_eth_token)
    ether_token.token_name.text = getString(R.string.ethereum_token_name)
    (ether_token.token_balance_placeholder as LottieAnimationView).playAnimation()
  }

  override fun updateTokenValue(tokenBalance: Balance) {
    if (tokenBalance.token.amount.compareTo(BigDecimal("-1")) == 1) {
      when (tokenBalance.token.currency) {
        APPC_C_CURRENCY -> {
          appcoins_credits_token.token_balance_placeholder.visibility = View.GONE
          (appcoins_credits_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
          appcoins_credits_token.token_balance.text =
              "${tokenBalance.token.amount} ${tokenBalance.token.symbol}"
          appcoins_credits_token.token_balance.visibility = View.VISIBLE
          appcoins_credits_token.token_balance_converted.text =
              "${tokenBalance.fiat.symbol}${tokenBalance.fiat.amount}"
          appcoins_credits_token.token_balance_converted.visibility = View.VISIBLE
        }
        APPC_CURRENCY -> {
          appcoins_token.token_balance_placeholder.visibility = View.GONE
          (appcoins_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
          appcoins_token.token_balance.text =
              "${tokenBalance.token.amount} ${tokenBalance.token.symbol}"
          appcoins_token.token_balance.visibility = View.VISIBLE
          appcoins_token.token_balance_converted.text =
              "${tokenBalance.fiat.symbol}${tokenBalance.fiat.amount}"
          appcoins_token.token_balance_converted.visibility = View.VISIBLE
        }
        ETH_CURRENCY -> {
          ether_token.token_balance_placeholder.visibility = View.GONE
          (ether_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
          ether_token.token_balance.text =
              "${tokenBalance.token.amount} ${tokenBalance.token.symbol}"
          ether_token.token_balance.visibility = View.VISIBLE
          ether_token.token_balance_converted.text =
              "${tokenBalance.fiat.symbol}${tokenBalance.fiat.amount}"
          ether_token.token_balance_converted.visibility = View.VISIBLE
        }
      }
    }
  }

  override fun updateOverallBalance(overallBalance: FiatValue) {
    if (overallBalance.amount.compareTo(BigDecimal("-1")) == 1) {
      balance_label_placeholder.visibility = View.GONE
      (balance_label_placeholder as LottieAnimationView).cancelAnimation()
      balance_label.text =
          String.format(getString(R.string.balance_total_body), overallBalance.currency)
      balance_label.visibility = View.VISIBLE

      balance_value_placeholder.visibility = View.GONE
      (balance_value_placeholder as LottieAnimationView).cancelAnimation()
      balance_value.text = overallBalance.symbol + overallBalance.amount
      balance_value.visibility = View.VISIBLE
    }
  }

  override fun getCreditClick(): Observable<Any> {
    return RxView.clicks(appcoins_credits_token)
  }

  override fun showCreditsDetails() {
    activityView?.showTokenDetailsScreen(
        TokenDetailsActivity.TokenDetailsId.APPC_CREDITS,
        appcoins_credits_token.token_icon, appcoins_credits_token.token_name,
        appcoins_credits_token)
  }

  override fun getAppcClick(): Observable<Any> {
    return RxView.clicks(appcoins_token)
  }

  override fun showAppcDetails() {
    activityView?.showTokenDetailsScreen(
        TokenDetailsActivity.TokenDetailsId.APPC,
        appcoins_token.token_icon, appcoins_token.token_name,
        appcoins_token)
  }

  override fun getEthClick(): Observable<Any> {
    return RxView.clicks(ether_token)
  }

  override fun showEthDetails() {
    activityView?.showTokenDetailsScreen(
        TokenDetailsActivity.TokenDetailsId.ETHER,
        ether_token.token_icon, ether_token.token_name,
        ether_token)
  }

  override fun getTopUpClick(): Observable<Any> {
    return RxView.clicks(top_up_btn)
  }

  override fun showTopUpScreen() {
    activityView?.showTopUpScreen()
  }

  private fun setAlpha(view: View, alphaPercentage: Float) {
    view.alpha = 1 - alphaPercentage * 1.20f
  }
}
