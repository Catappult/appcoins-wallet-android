package com.asfoundation.wallet.ui.wallets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.RemoveWalletFirstLayoutBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@AndroidEntryPoint
class RemoveWalletFragment : BasePageViewFragment(), RemoveWalletView {

  private lateinit var presenter: RemoveWalletPresenter
  private lateinit var activityView: RemoveWalletActivityView

  private val views by viewBinding(RemoveWalletFirstLayoutBinding::bind)

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
                            savedInstanceState: Bundle?): View = RemoveWalletFirstLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setWalletBalance()
    presenter.present()
  }

  override fun backUpWalletClick() = RxView.clicks(views.backupButton)

  override fun noBackUpWalletClick() = RxView.clicks(views.noBackupButton)

  override fun navigateToBackUp() = activityView.navigateToBackUp(walletAddress)

  override fun proceedWithRemoveWallet() = activityView.navigateToWalletRemoveConfirmation()

  private fun setWalletBalance() {
    views.walletCard.walletAddress.text = walletAddress
    views.walletCard.walletBalance.text = fiatBalance
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private val walletAddress: String by lazy {
    if (requireArguments().containsKey(WALLET_ADDRESS_KEY)) {
      requireArguments().getString(WALLET_ADDRESS_KEY)!!
    } else {
      throw IllegalArgumentException("walletAddress not found")
    }
  }

  private val fiatBalance: String by lazy {
    if (requireArguments().containsKey(FIAT_BALANCE_KEY)) {
      requireArguments().getString(FIAT_BALANCE_KEY)!!
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
