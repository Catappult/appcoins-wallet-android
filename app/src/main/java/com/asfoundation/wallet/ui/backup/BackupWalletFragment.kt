package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.iab.FiatValue
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_backup_wallet_layout.*
import kotlinx.android.synthetic.main.item_wallet_addr.*
import javax.inject.Inject

class BackupWalletFragment : DaggerFragment(), BackupWalletFragmentView {

  @Inject
  lateinit var balanceInteract: BalanceInteract
  private lateinit var fragmentContainer: ViewGroup
  private lateinit var presenter: BackupWalletFragmentPresenter
  private lateinit var activityView: BackupActivityView

  companion object {
    private const val PARAM_WALLET_ADDR = "PARAM_WALLET_ADDR"

    @JvmStatic
    fun newInstance(walletAddress: String): BackupWalletFragment {
      val bundle = Bundle()
      bundle.putString(PARAM_WALLET_ADDR, walletAddress)
      val fragment = BackupWalletFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  private val walletAddr: String by lazy {
    if (arguments!!.containsKey(PARAM_WALLET_ADDR)) {
      arguments!!.getString(PARAM_WALLET_ADDR)!!
    } else {
      throw IllegalArgumentException("Wallet address not available")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BackupWalletFragmentPresenter(balanceInteract, this, activityView, Schedulers.io(),
        AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_backup_wallet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(walletAddr)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is BackupActivityView) { "TopUp fragment must be attached to TopUp activity" }
    activityView = context
  }

  override fun showBalance(value: FiatValue) {
    address.text = walletAddr
    amount.text = getString(R.string.value_fiat, value.symbol, value.amount)

  }

  override fun getBackupClick(): Observable<String> {
    return RxView.clicks(backup_btn).map { password.text.toString() }
  }
}
