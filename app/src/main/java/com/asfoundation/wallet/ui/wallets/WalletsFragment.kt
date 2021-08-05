package com.asfoundation.wallet.ui.wallets

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.main.MainActivityNavigator
import com.asfoundation.wallet.my_wallets.MyWalletsNavigator
import com.asfoundation.wallet.ui.balance.BalanceFragmentView
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.active_wallet_card.*
import kotlinx.android.synthetic.main.active_wallet_card.view.*
import kotlinx.android.synthetic.main.fragment_wallets_bottom_sheet.*
import kotlinx.android.synthetic.main.restore_create_buttons_layout.*
import javax.inject.Inject

class WalletsFragment : DaggerFragment(), WalletsView {

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var walletsInteract: WalletsInteract

  lateinit var myWalletsNavigator: MyWalletsNavigator

  @Inject
  lateinit var currencyFormatter: CurrencyFormatUtils

  @Inject
  lateinit var logger: Logger
  private var uiEventListener: PublishSubject<String>? = null
  private lateinit var adapter: WalletsAdapter
  private lateinit var presenter: WalletsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    uiEventListener = PublishSubject.create()
    presenter = WalletsPresenter(this, walletsInteract, logger, CompositeDisposable(),
        AndroidSchedulers.mainThread(), Schedulers.io(), walletsEventSender)
    myWalletsNavigator = MyWalletsNavigator(this, MainActivityNavigator(requireContext()))
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_wallets_bottom_sheet, container, false)
  }

  @SuppressLint("SetTextI18n")
  override fun setupUi(totalWallets: Int, totalBalance: FiatValue,
                       walletsBalanceList: List<WalletBalance>) {
    total_wallets.text = totalWallets.toString()
    total_wallets.visibility = View.VISIBLE
    wallets_skeleton.visibility = View.GONE
    accumulated_value.text =
        totalBalance.symbol + currencyFormatter.formatCurrency(totalBalance.amount)
    accumulated_value.visibility = View.VISIBLE
    accumulated_value_skeleton.visibility = View.GONE

    val currentWalletBalance = getCurrentWalletBalance(walletsBalanceList)
    active_wallet_address.text = currentWalletBalance.walletAddress
    active_wallet_card.wallet_balance.text = getString(
        R.string.wallets_2nd_view_balance_title) + " " + totalBalance.symbol +
        currencyFormatter.formatCurrency(currentWalletBalance.balance.amount)


    val adapterList = removeCurrentWallet(walletsBalanceList)
    adapter =
        WalletsAdapter(requireContext(), adapterList, uiEventListener!!, currencyFormatter,
            WalletsViewType.BALANCE)
    other_wallets_cards_recycler.adapter = adapter
    val walletsText =
        resources.getQuantityString(R.plurals.wallets_bottom_wallets_title, walletsBalanceList.size)
    wallets_text.text = walletsText
    if (adapterList.isEmpty()) other_wallets_header.visibility = View.INVISIBLE
    else other_wallets_header.visibility = View.VISIBLE
  }

  override fun otherWalletCardClicked() = uiEventListener!!

  override fun activeWalletCardClicked(): Observable<String> = RxView.clicks(active_wallet_card)
      .map { active_wallet_address.text.toString() }

  override fun restoreWalletClicked(): Observable<Any> = RxView.clicks(restore_button_layout)

  override fun createNewWalletClicked(): Observable<Any> = RxView.clicks(create_new_button_layout)

  override fun navigateToRestoreView() = myWalletsNavigator.navigateToRestoreView()

  override fun showCreatingAnimation() {
    val parentFragment = provideParentFragment()
    parentFragment?.showCreatingAnimation()
  }

  override fun showWalletCreatedAnimation() {
    val parentFragment = provideParentFragment()
    parentFragment?.showWalletCreatedAnimation()
  }

  override fun navigateToWalletDetailView(walletAddress: String, isActive: Boolean) {
    myWalletsNavigator.navigateToWalletDetailView(walletAddress, isActive)
  }


  override fun onBottomSheetHeaderClicked() = RxView.clicks(bottom_sheet_header)

  override fun changeBottomSheetState() {
    val parentFragment = provideParentFragment()
    parentFragment?.changeBottomSheetState()
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

  private fun provideParentFragment(): BalanceFragmentView? {
    if (parentFragment !is BalanceFragmentView) {
      return null
    }
    return parentFragment as BalanceFragmentView
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}