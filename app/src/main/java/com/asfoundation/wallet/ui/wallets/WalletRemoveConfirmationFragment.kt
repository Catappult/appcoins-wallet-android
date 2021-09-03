package com.asfoundation.wallet.ui.wallets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.interact.DeleteWalletInteract
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.remove_wallet_balance.*
import kotlinx.android.synthetic.main.remove_wallet_second_layout.*
import javax.inject.Inject

class WalletRemoveConfirmationFragment : BasePageViewFragment(), WalletRemoveConfirmationView {

  @Inject
  lateinit var deleteWalletInteract: DeleteWalletInteract

  @Inject
  lateinit var logger: Logger
  private lateinit var presenter: WalletRemoveConfirmationPresenter
  private lateinit var activityView: RemoveWalletActivityView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = WalletRemoveConfirmationPresenter(this, walletAddress, deleteWalletInteract, logger,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is RemoveWalletActivityView) {
      throw IllegalStateException(
          "Wallet Confirmation must be attached to Remove Wallet Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.remove_wallet_second_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setWalletBalance()
    presenter.present()
  }

  override fun noButtonClick() = RxView.clicks(no_remove_wallet_button)

  override fun yesButtonClick() = RxView.clicks(yes_remove_wallet_button)

  override fun navigateBack() {
    activity?.onBackPressed()
  }

  override fun showRemoveWalletAnimation() = activityView.showRemoveWalletAnimation()

  override fun finish() = activityView.navigateToWalletList()

  override fun showAuthentication() = activityView.showAuthentication()

  override fun authenticationResult() = activityView.authenticationResult()

  private fun setWalletBalance() {
    wallet_address.text = walletAddress
    wallet_balance.text = fiatBalance
    balance_appcoins.text = appcoinsBalance
    balance_credits.text = creditsBalance
    balance_ethereum.text = ethereumBalance
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private val walletAddress: String by lazy {
    if (arguments!!.containsKey(WALLET_ADDRESS_KEY)) {
      arguments!!.getString(WALLET_ADDRESS_KEY)!!
    } else {
      throw IllegalArgumentException("walletAddress not found")
    }
  }

  private val fiatBalance: String by lazy {
    if (arguments!!.containsKey(FIAT_BALANCE_KEY)) {
      arguments!!.getString(FIAT_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("fiat balance not found")
    }
  }

  private val appcoinsBalance: String by lazy {
    if (arguments!!.containsKey(APPC_BALANCE_KEY)) {
      arguments!!.getString(APPC_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("appc balance not found")
    }
  }

  private val creditsBalance: String by lazy {
    if (arguments!!.containsKey(CREDITS_BALANCE_KEY)) {
      arguments!!.getString(CREDITS_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("credits balance not found")
    }
  }

  private val ethereumBalance: String by lazy {
    if (arguments!!.containsKey(ETHEREUM_BALANCE_KEY)) {
      arguments!!.getString(ETHEREUM_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("ethereum balance not found")
    }
  }

  companion object {

    private const val WALLET_ADDRESS_KEY = "wallet_address"
    private const val FIAT_BALANCE_KEY = "fiat_balance"
    private const val APPC_BALANCE_KEY = "appc_balance"
    private const val CREDITS_BALANCE_KEY = "credits_balance"
    private const val ETHEREUM_BALANCE_KEY = "ethereum_balance"

    fun newInstance(walletAddress: String, totalFiatBalance: String,
                    appcoinsBalance: String, creditsBalance: String,
                    ethereumBalance: String): WalletRemoveConfirmationFragment {
      val fragment = WalletRemoveConfirmationFragment()
      Bundle().apply {
        putString(WALLET_ADDRESS_KEY, walletAddress)
        putString(FIAT_BALANCE_KEY, totalFiatBalance)
        putString(APPC_BALANCE_KEY, appcoinsBalance)
        putString(CREDITS_BALANCE_KEY, creditsBalance)
        putString(ETHEREUM_BALANCE_KEY, ethereumBalance)
        fragment.arguments = this
      }
      return fragment
    }
  }
}
