package com.asfoundation.wallet.ui.wallets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.balance.BalanceActivityView
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import com.asfoundation.wallet.util.generateQrCode
import com.asfoundation.wallet.util.scaleToString
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.qr_code_layout.*
import kotlinx.android.synthetic.main.qr_code_layout.qr_image
import kotlinx.android.synthetic.main.wallet_detail_balance_layout.*
import kotlinx.android.synthetic.main.wallet_detail_layout.*
import javax.inject.Inject

class WalletDetailFragment : DaggerFragment(), WalletDetailView {

  @Inject
  lateinit var interactor: WalletDetailInteractor
  private lateinit var activityView: BalanceActivityView
  private lateinit var presenter: WalletDetailPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = WalletDetailPresenter(this, interactor, walletAddress, CompositeDisposable(),
        AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is BalanceActivityView) {
      throw IllegalStateException(
          "Wallet Detail Fragment must be attached to Balance Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.wallet_detail_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activityView.setupToolbar()
    try {
      val mergedQrCode = walletAddress.generateQrCode(resources, activity!!.windowManager)
      qr_image.setImageBitmap(mergedQrCode)
    } catch (e: Exception) {
      Snackbar.make(main_layout, getString(R.string.error_fail_generate_qr), Snackbar.LENGTH_SHORT)
          .show()
    }
    wallet_address.text = walletAddress
    presenter.present()
  }

  @SuppressLint("SetTextI18n")
  override fun populateUi(balanceScreenModel: BalanceScreenModel) {
    val fiat = balanceScreenModel.overallFiat
    val appc = balanceScreenModel.appcBalance.token
    val credits = balanceScreenModel.creditsBalance.token
    val ethereum = balanceScreenModel.ethBalance.token
    total_balance_fiat.text = fiat.symbol + fiat.amount.scaleToString(2)
    balance_appcoins.text = appc.amount.scaleToString(2) + " " + appc.symbol
    balance_credits.text = credits.amount.scaleToString(2) + " " + credits.symbol
    balance_ethereum.text = ethereum.amount.scaleToString(4) + " " + ethereum.symbol
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.stop()
  }

  companion object {

    private const val WALLET_ADDRESS_KEY = "wallet_address"
    private const val IS_ACTIVE_KEY = "is_active"

    fun newInstance(walletAddress: String, isActive: Boolean): WalletDetailFragment {
      val bundle = Bundle()
      val fragment = WalletDetailFragment()
      bundle.putString(WALLET_ADDRESS_KEY, walletAddress)
      bundle.putBoolean(IS_ACTIVE_KEY, isActive)
      fragment.arguments = bundle
      return fragment
    }
  }

  private val walletAddress: String by lazy {
    if (arguments!!.containsKey(WALLET_ADDRESS_KEY)) {
      arguments!!.getString(WALLET_ADDRESS_KEY)
    } else {
      throw IllegalArgumentException("walletAddress not found")
    }
  }

  private val isActive: Boolean by lazy {
    if (arguments!!.containsKey(IS_ACTIVE_KEY)) {
      arguments!!.getBoolean(IS_ACTIVE_KEY)
    } else {
      throw IllegalArgumentException("is active not found")
    }
  }
}
