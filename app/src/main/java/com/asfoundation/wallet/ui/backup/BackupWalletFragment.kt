package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_backup_wallet_layout.*
import kotlinx.android.synthetic.main.item_wallet_addr.*
import javax.inject.Inject

class BackupWalletFragment : DaggerFragment(), BackupWalletFragmentView {

  @Inject
  lateinit var balanceInteract: BalanceInteract

  @Inject
  lateinit var currencyFormatter: CurrencyFormatUtils
  private lateinit var presenter: BackupWalletPresenter
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

  private val walletAddress: String by lazy {
    if (arguments!!.containsKey(PARAM_WALLET_ADDR)) {
      arguments!!.getString(PARAM_WALLET_ADDR)!!
    } else {
      throw IllegalArgumentException("Wallet address not available")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        BackupWalletPresenter(balanceInteract, this, activityView,
            CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_backup_wallet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(walletAddress)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is BackupActivityView) { "BackupWallet fragment must be attached to Backup activity" }
    activityView = context
  }

  override fun showBalance(value: FiatValue) {
    address.text = walletAddress
    amount.text =
        getString(R.string.value_fiat, value.symbol, currencyFormatter.formatCurrency(value.amount))
  }

  override fun getBackupClick(): Observable<String> = RxView.clicks(backup_btn)
      .map { password.text.toString() }

  override fun hideKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(password.windowToken, 0)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
