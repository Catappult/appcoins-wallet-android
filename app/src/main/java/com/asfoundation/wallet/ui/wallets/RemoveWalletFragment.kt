package com.asfoundation.wallet.ui.wallets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.remove_wallet_first_layout.*
import kotlinx.android.synthetic.main.wallet_outlined_card.*

class RemoveWalletFragment : BasePageViewFragment(), RemoveWalletView {

  private lateinit var presenter: RemoveWalletPresenter
  private lateinit var activityView: RemoveWalletActivityView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = RemoveWalletPresenter(this,
        CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is RemoveWalletActivityView) {
      throw IllegalStateException(
          "Remove Wallet must be attached to Remove Wallet Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.remove_wallet_first_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setWalletBalance()
    presenter.present()
  }

  override fun backUpWalletClick() = RxView.clicks(backup_button)

  override fun noBackUpWalletClick() = RxView.clicks(no_backup_button)

  override fun navigateToBackUp() = activityView.navigateToBackUp(walletAddress)

  override fun proceedWithRemoveWallet() = activityView.navigateToWalletRemoveConfirmation()

  private fun setWalletBalance() {
    wallet_address.text = walletAddress
    wallet_balance.text = fiatBalance
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

  companion object {

    private const val WALLET_ADDRESS_KEY = "wallet_address"
    private const val FIAT_BALANCE_KEY = "fiat_balance"

    fun newInstance(walletAddress: String, totalFiatBalance: String): RemoveWalletFragment {
      val fragment = RemoveWalletFragment()
      Bundle().apply {
        putString(WALLET_ADDRESS_KEY, walletAddress)
        putString(FIAT_BALANCE_KEY, totalFiatBalance)
        fragment.arguments = this
      }
      return fragment
    }
  }
}
