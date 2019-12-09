package com.asfoundation.wallet.ui.wallets

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.scaleToString
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.active_wallet_card.*
import kotlinx.android.synthetic.main.active_wallet_card.view.*
import kotlinx.android.synthetic.main.wallets_layout.*
import javax.inject.Inject

class WalletsFragment : DaggerFragment(),
    WalletsView {

  @Inject
  lateinit var walletsInteract: WalletsInteract
  private var uiEventListener: PublishSubject<String>? = null
  private lateinit var adapter: NewWalletsAdapter
  private lateinit var presenter: WalletsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    uiEventListener = PublishSubject.create()
    presenter = WalletsPresenter(this, walletsInteract, CompositeDisposable(),
        AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.wallets_layout, container, false)
  }

  @SuppressLint("SetTextI18n")
  override fun setupUi(totalWallets: Int, totalBalance: FiatValue,
                       walletsBalanceList: List<WalletBalance>) {
    total_wallets.text = totalWallets.toString()
    total_wallets.visibility = View.VISIBLE
    wallets_skeleton.visibility = View.GONE
    accumulated_value.text = totalBalance.symbol + totalBalance.amount.scaleToString(2)
    accumulated_value.visibility = View.VISIBLE
    accumulated_value_skeleton.visibility = View.GONE

    val currentWalletBalance = getCurrentWalletBalance(walletsBalanceList)
    active_wallet_address.text = currentWalletBalance.walletAddress
    active_wallet_card.wallet_balance.text = getString(
        R.string.wallets_2nd_view_balance_title) + " " + totalBalance.symbol +
        currentWalletBalance.balance.amount.scaleToString(2)


    val layoutManager = LinearLayoutManager(context)
    layoutManager.orientation = RecyclerView.VERTICAL
    adapter =
        NewWalletsAdapter(context!!, removeCurrentWallet(walletsBalanceList), uiEventListener!!)
    other_wallets_cards_recycler.layoutManager = layoutManager
    other_wallets_cards_recycler.adapter = adapter
  }

  override fun otherWalletCardClicked(): Observable<String> {
    return uiEventListener!!
  }

  override fun activeWalletCardClicked(): Observable<String> {
    return RxView.clicks(active_wallet_card)
        .map { active_wallet_address.text.toString() }
  }

  override fun navigateToWalletDetailView(walletAddress: String, isActive: Boolean) {
    fragmentManager?.beginTransaction()
        ?.replace(R.id.bottom_sheet_fragment_container,
            WalletDetailFragment.newInstance(walletAddress, isActive))
        ?.commit()
  }

  private fun removeCurrentWallet(walletsBalanceList: List<WalletBalance>): List<WalletBalance> {
    val otherWalletsBalanceList = ArrayList<WalletBalance>()
    for (balance in walletsBalanceList) {
      if (!balance.isActiveWallet) otherWalletsBalanceList.add(balance)
    }
    return otherWalletsBalanceList
  }

  private fun getCurrentWalletBalance(
      walletBalanceList: List<WalletBalance>): WalletBalance {
    for (walletBalance in walletBalanceList) {
      if (walletBalance.isActiveWallet) return walletBalance
    }
    return WalletBalance()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }

  companion object {
    fun newInstance(): WalletsFragment {
      return WalletsFragment()
    }
  }
}