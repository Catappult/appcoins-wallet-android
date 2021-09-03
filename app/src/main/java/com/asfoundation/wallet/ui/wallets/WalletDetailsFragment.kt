package com.asfoundation.wallet.ui.wallets

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.content.res.ResourcesCompat
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.MyAddressActivity
import com.asfoundation.wallet.ui.balance.BalanceActivityView
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.generateQrCode
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.copy_share_buttons_layout.*
import kotlinx.android.synthetic.main.fragment_wallet_details.*
import kotlinx.android.synthetic.main.remove_backup_buttons_layout.*
import kotlinx.android.synthetic.main.wallet_details_balance_layout.*
import javax.inject.Inject

class WalletDetailsFragment : BasePageViewFragment(), WalletDetailsView {

  @Inject
  lateinit var interactor: WalletDetailsInteractor

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var currencyFormatter: CurrencyFormatUtils
  private lateinit var activityView: BalanceActivityView
  private lateinit var presenter: WalletDetailsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = WalletDetailsPresenter(this, interactor, walletsEventSender, walletAddress,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
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
    return inflater.inflate(R.layout.fragment_wallet_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activityView.setupToolbar()

    generateQrCode(view)

    wallet_address.text = walletAddress

    handleActiveWalletLayoutVisibility()

    presenter.present()
  }

  override fun copyClick() = RxView.clicks(copy_button)

  override fun shareClick() = RxView.clicks(share_button)

  override fun removeWalletClick() = RxView.clicks(remove_button_layout)

  override fun backupInactiveWalletClick() = RxView.clicks(backup_button_layout)

  override fun backupActiveWalletClick() = RxView.clicks(middle_backup_button_layout)

  override fun makeWalletActiveClick() = RxView.clicks(make_this_active_button)

  override fun setAddressToClipBoard(walletAddress: String) {
    activity?.let {
      val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
      val clip = ClipData.newPlainText(
          MyAddressActivity.KEY_ADDRESS, walletAddress)
      clipboard?.setPrimaryClip(clip)
      view?.let { view ->
        Snackbar.make(view, R.string.wallets_address_copied_body, Snackbar.LENGTH_SHORT)
            .show()
      }
    }
  }

  override fun showShare(walletAddress: String) {
    activity?.let {
      ShareCompat.IntentBuilder.from(activity!!)
          .setText(walletAddress)
          .setType("text/plain")
          .setChooserTitle(resources.getString(R.string.share_via))
          .startChooser()
    }
  }

  override fun navigateToBalanceView() {
    activityView.showBalanceScreen()
  }

  override fun navigateToBackupView(walletAddress: String) {
    activityView.navigateToBackupView(walletAddress)
  }

  override fun navigateToRemoveWalletView(walletAddress: String) {
    activityView.navigateToRemoveWalletView(walletAddress, total_balance_fiat.text.toString(),
        balance_appcoins.text.toString(), balance_credits.text.toString(),
        balance_ethereum.text.toString())
  }

  @SuppressLint("SetTextI18n")
  override fun populateUi(balanceScreenModel: BalanceScreenModel) {
    val fiat = balanceScreenModel.overallFiat
    val appc = balanceScreenModel.appcBalance.token
    val credits = balanceScreenModel.creditsBalance.token
    val ethereum = balanceScreenModel.ethBalance.token
    total_balance_fiat.text = fiat.symbol + currencyFormatter.formatCurrency(fiat.amount)
    balance_appcoins.text =
        currencyFormatter.formatCurrency(appc.amount, WalletCurrency.APPCOINS) + " " + appc.symbol
    balance_credits.text = currencyFormatter.formatCurrency(credits.amount,
        WalletCurrency.CREDITS) + " " + credits.symbol
    balance_ethereum.text = currencyFormatter.formatCurrency(ethereum.amount,
        WalletCurrency.ETHEREUM) + " " + ethereum.symbol
  }

  private fun handleActiveWalletLayoutVisibility() {
    if (isActive) {
      active_wallet_info.visibility = View.VISIBLE
      middle_backup_button_layout.visibility = View.VISIBLE
    } else {
      remove_backup_buttons.visibility = View.VISIBLE
      make_this_active_button.visibility = View.VISIBLE
    }
  }

  private fun generateQrCode(view: View) {
    try {
      val logo = ResourcesCompat.getDrawable(resources, R.drawable.ic_appc_token, null)
      val mergedQrCode = walletAddress.generateQrCode(activity!!.windowManager, logo!!)
      qr_image.setImageBitmap(mergedQrCode)
    } catch (e: Exception) {
      Snackbar.make(view, getString(R.string.error_fail_generate_qr), Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    private const val WALLET_ADDRESS_KEY = "wallet_address"
    private const val IS_ACTIVE_KEY = "is_active"

    fun newInstance(walletAddress: String, isActive: Boolean): WalletDetailsFragment {
      val bundle = Bundle()
      val fragment = WalletDetailsFragment()
      bundle.putString(WALLET_ADDRESS_KEY, walletAddress)
      bundle.putBoolean(IS_ACTIVE_KEY, isActive)
      fragment.arguments = bundle
      return fragment
    }
  }

  private val walletAddress: String by lazy {
    if (arguments!!.containsKey(WALLET_ADDRESS_KEY)) {
      arguments!!.getString(WALLET_ADDRESS_KEY)!!
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
