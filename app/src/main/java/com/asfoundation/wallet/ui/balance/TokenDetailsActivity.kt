package com.asfoundation.wallet.ui.balance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import android.view.View
import android.view.Window
import com.asf.wallet.R
import com.asfoundation.wallet.router.TopUpRouter
import com.asfoundation.wallet.ui.BaseActivity
import com.appcoins.wallet.core.utils.common.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_token_details.*

@AndroidEntryPoint
class TokenDetailsActivity : BaseActivity(), TokenDetailsView {

  private var contentVisible = false
  private lateinit var token: TokenDetailsId
  private lateinit var presenter: TokenDetailsPresenter

  override fun onResume() {
    super.onResume()
    sendPageViewEvent()
  }

  enum class TokenDetailsId {
    ETHER, APPC, APPC_CREDITS
  }

  companion object {
    @JvmStatic
    fun newInstance(context: Context, tokenDetailsId: TokenDetailsId): Intent {
      val intent = Intent(context, TokenDetailsActivity::class.java)
      intent.putExtra(KEY_CONTENT, tokenDetailsId)
      return intent
    }

    private const val KEY_CONTENT = "KEY_CONTENT"
    private const val PARAM_ENTERING = "PARAM_ENTERING"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    setContentView(R.layout.activity_token_details)
    presenter = TokenDetailsPresenter(this, CompositeDisposable())
    savedInstanceState?.let {
      contentVisible = it.getBoolean(PARAM_ENTERING)
    }
    presenter.present()
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(PARAM_ENTERING, contentVisible)
  }

  private fun setContent(tokenDetailsId: TokenDetailsId) {
    when (tokenDetailsId) {
      TokenDetailsId.ETHER -> {
        token_icon.setImageResource(R.drawable.ic_eth_token)
        token_name.text = getString(R.string.ethereum_token_name)
        token_symbol.text = "(${WalletCurrency.ETHEREUM.symbol})"
        token_description.text = getString(R.string.balance_ethereum_body)
      }
      TokenDetailsId.APPC -> {
        token_icon.setImageResource(R.drawable.ic_appc_token)
        token_name.text = getString(R.string.appc_token_name)
        token_symbol.text = "(${WalletCurrency.APPCOINS.symbol})"
        token_description.text = getString(R.string.balance_appcoins_body)
      }
      TokenDetailsId.APPC_CREDITS -> {
        token_icon.setImageResource(R.drawable.ic_appc_c_token)
        token_name.text = getString(R.string.appc_credits_token_name)
        token_symbol.text = "(${WalletCurrency.CREDITS.symbol})"
        token_description.text = getString(R.string.balance_appccreditos_body)
      }
    }
  }

  override fun onBackPressed() {
    token_symbol.visibility = View.INVISIBLE
    token_description.visibility = View.INVISIBLE
    close_btn.visibility = View.INVISIBLE
    topup_btn.visibility = View.INVISIBLE
    super.onBackPressed()
  }

  override fun setupUi() {
    intent.extras?.let {
      if (it.containsKey(
              KEY_CONTENT)) {
        token = it.getSerializable(KEY_CONTENT) as TokenDetailsId
        setContent(token)
      }
    }

    val sharedElementEnterTransition = window.sharedElementEnterTransition
    sharedElementEnterTransition.addListener(object : Transition.TransitionListener {
      override fun onTransitionStart(transition: Transition) {
      }

      override fun onTransitionEnd(transition: Transition) {
        if (!contentVisible) {
          token_symbol.visibility = View.VISIBLE
          token_description.visibility = View.VISIBLE
          close_btn.visibility = View.VISIBLE
          if (token == TokenDetailsId.APPC_CREDITS) {
            topup_btn.visibility = View.VISIBLE
          }
          contentVisible = true
        }
      }

      override fun onTransitionCancel(transition: Transition) {
      }

      override fun onTransitionPause(transition: Transition) {
      }

      override fun onTransitionResume(transition: Transition) {
      }
    })

    if (contentVisible) {
      token_symbol.visibility = View.VISIBLE
      token_description.visibility = View.VISIBLE
      close_btn.visibility = View.VISIBLE
      if (token == TokenDetailsId.APPC_CREDITS) {
        topup_btn.visibility = View.VISIBLE
      }
    }
  }

  override fun getOkClick(): Observable<Any> {
    return RxView.clicks(close_btn)
  }

  override fun close() {
    onBackPressed()
  }

  override fun getTopUpClick(): Observable<Any> {
    return RxView.clicks(topup_btn)
  }

  override fun showTopUp() {
    TopUpRouter().open(this)
  }
}
