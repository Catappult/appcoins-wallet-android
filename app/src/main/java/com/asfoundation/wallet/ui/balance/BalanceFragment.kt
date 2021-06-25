package com.asfoundation.wallet.ui.balance

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.MyAddressActivity
import com.asfoundation.wallet.ui.wallets.WalletsFragment
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.convertDpToPx
import com.asfoundation.wallet.verification.VerificationActivity
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.backup_tooltip.view.*
import kotlinx.android.synthetic.main.balance_token_item.view.*
import kotlinx.android.synthetic.main.fragment_balance.*
import kotlinx.android.synthetic.main.fragment_balance.bottom_sheet_fragment_container
import kotlinx.android.synthetic.main.invite_friends_fragment_layout.*
import kotlinx.android.synthetic.main.layout_code_requested.*
import kotlinx.android.synthetic.main.layout_unverified.*
import javax.inject.Inject
import kotlin.math.abs

class BalanceFragment : BasePageViewFragment(), BalanceFragmentView {

  @Inject
  lateinit var balanceInteractor: BalanceInteractor

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private var onBackPressedSubject: PublishSubject<Any>? = null
  private var activityView: BalanceActivityView? = null
  private var showingAnimation: Boolean = false
  private var popup: PopupWindow? = null
  private lateinit var tooltip: View
  private lateinit var walletsBottomSheet: BottomSheetBehavior<View>
  private lateinit var presenter: BalanceFragmentPresenter

  companion object {
    @JvmStatic
    fun newInstance() = BalanceFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is BalanceActivityView) {
      throw IllegalStateException("Balance Fragment must be attached to Balance Activity")
    }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = BalanceFragmentPresenter(this, activityView, balanceInteractor, walletsEventSender,
        Schedulers.io(), AndroidSchedulers.mainThread(), CompositeDisposable(), formatter)
    onBackPressedSubject = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    tooltip = layoutInflater.inflate(R.layout.backup_tooltip, null)
    childFragmentManager.beginTransaction()
        .replace(R.id.bottom_sheet_fragment_container, WalletsFragment())
        .commit()
    return inflater.inflate(R.layout.fragment_balance, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    walletsBottomSheet =
        BottomSheetBehavior.from(bottom_sheet_fragment_container)
    setBackListener(view)
    activityView?.let {
      if (it.shouldExpandBottomSheet()) walletsBottomSheet.state =
          BottomSheetBehavior.STATE_EXPANDED
    }
    animateBackgroundFade()
    activityView?.setupToolbar()
    presenter.present()

    (app_bar as AppBarLayout).addOnOffsetChangedListener(
        AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
          if (balance_label != null) {
            val percentage = abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            setAlpha(balance_label, percentage)
            setAlpha(balance_value, percentage)
            setAlpha(balance_label_placeholder, percentage)
            setAlpha(balance_value_placeholder, percentage)
          }
        })
  }

  override fun getBottomSheetStateChanged(): Observable<Int> {
    return Observable.create {
      val callback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
          it.onNext(newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
      }
      walletsBottomSheet.addBottomSheetCallback(callback)
      it.setCancellable { walletsBottomSheet.removeBottomSheetCallback(callback) }
    }
  }

  override fun onResume() {
    super.onResume()
    presenter.onResume()
  }

  override fun setTooltip() {
    popup = PopupWindow(tooltip)
    popup?.height = ViewGroup.LayoutParams.WRAP_CONTENT
    popup?.width = ViewGroup.LayoutParams.MATCH_PARENT
    val offset = 25.convertDpToPx(resources)
    faded_background.visibility = View.VISIBLE
    popup?.showAsDropDown(backup_active_button, 0, offset * -1)
  }

  override fun onDestroyView() {
    activityView?.enableBack()
    presenter.stop()
    super.onDestroyView()
  }

  override fun setupUI() {
    balance_value_placeholder.playAnimation()
    balance_label_placeholder.playAnimation()

    appcoins_credits_token.token_icon.setImageResource(R.drawable.ic_appc_c_token)
    appcoins_credits_token.token_name.text = getString(R.string.appc_credits_token_name)
    (appcoins_credits_token.token_balance_placeholder as LottieAnimationView).playAnimation()

    appcoins_token.token_icon.setImageResource(R.drawable.ic_appc_token)
    appcoins_token.token_name.text = getString(R.string.appc_token_name)
    (appcoins_token.token_balance_placeholder as LottieAnimationView).playAnimation()

    ether_token.token_icon.setImageResource(R.drawable.ic_eth_token)
    ether_token.token_name.text = getString(R.string.ethereum_token_name)
    (ether_token.token_balance_placeholder as LottieAnimationView).playAnimation()
  }

  @SuppressLint("SetTextI18n")
  override fun updateTokenValue(tokenBalance: String,
                                fiatBalance: String,
                                tokenCurrency: WalletCurrency,
                                fiatCurrency: String) {
    if (tokenBalance != "-1" && fiatBalance != "-1") {
      when (tokenCurrency) {
        WalletCurrency.CREDITS -> {
          appcoins_credits_token.token_balance_placeholder.visibility = View.GONE
          (appcoins_credits_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
          appcoins_credits_token.token_balance.text =
              "$tokenBalance ${tokenCurrency.symbol}"
          appcoins_credits_token.token_balance.visibility = View.VISIBLE
          appcoins_credits_token.token_balance_converted.text =
              "$fiatCurrency$fiatBalance"
          appcoins_credits_token.token_balance_converted.visibility = View.VISIBLE
        }
        WalletCurrency.APPCOINS -> {
          appcoins_token.token_balance_placeholder.visibility = View.GONE
          (appcoins_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
          appcoins_token.token_balance.text =
              "$tokenBalance ${tokenCurrency.symbol}"
          appcoins_token.token_balance.visibility = View.VISIBLE
          appcoins_token.token_balance_converted.text =
              "$fiatCurrency$fiatBalance"
          appcoins_token.token_balance_converted.visibility = View.VISIBLE
        }
        WalletCurrency.ETHEREUM -> {
          ether_token.token_balance_placeholder.visibility = View.GONE
          (ether_token.token_balance_placeholder as LottieAnimationView).cancelAnimation()
          ether_token.token_balance.text =
              "$tokenBalance ${tokenCurrency.symbol}"
          ether_token.token_balance.visibility = View.VISIBLE
          ether_token.token_balance_converted.text =
              "$fiatCurrency$fiatBalance"
          ether_token.token_balance_converted.visibility = View.VISIBLE
        }
        else -> return
      }
    }
  }

  @SuppressLint("SetTextI18n")
  override fun updateOverallBalance(overallBalance: String, currency: String, symbol: String) {
    if (overallBalance != "-1") {
      balance_label_placeholder.visibility = View.GONE
      (balance_label_placeholder as LottieAnimationView).cancelAnimation()
      balance_label.text =
          String.format(getString(R.string.balance_total_body), currency)
      balance_label.visibility = View.VISIBLE

      balance_value_placeholder.visibility = View.GONE
      (balance_value_placeholder as LottieAnimationView).cancelAnimation()
      balance_value.text = symbol + overallBalance
      balance_value.visibility = View.VISIBLE
    }
  }

  override fun getCreditClick(): Observable<View> = RxView.clicks(appcoins_credits_token)
      .map { appcoins_credits_token }

  override fun getAppcClick(): Observable<View> = RxView.clicks(appcoins_token)
      .map { appcoins_token }

  override fun getEthClick(): Observable<View> = RxView.clicks(ether_token)
      .map { ether_token }

  override fun showTokenDetails(view: View) {
    lateinit var tokenId: TokenDetailsActivity.TokenDetailsId
    when (view) {
      appcoins_credits_token -> tokenId = TokenDetailsActivity.TokenDetailsId.APPC_CREDITS
      appcoins_token -> tokenId = TokenDetailsActivity.TokenDetailsId.APPC
      ether_token -> tokenId = TokenDetailsActivity.TokenDetailsId.ETHER
    }

    activityView?.showTokenDetailsScreen(tokenId, view.token_icon, view.token_name, view)
  }

  override fun getVerifyWalletClick() = RxView.clicks(verify_wallet_button)

  override fun getInsertCodeClick() = RxView.clicks(insert_code_button)

  override fun getCopyClick() = RxView.clicks(copy_address)

  override fun getQrCodeClick() = RxView.clicks(wallet_qr_code)

  override fun getBackupClick() = RxView.clicks(backup_active_button)

  override fun getTooltipDismissClick() = RxView.clicks(tooltip.tooltip_later_button)

  override fun getTooltipBackupButton() = RxView.clicks(tooltip.tooltip_backup_button)

  override fun setWalletAddress(walletAddress: String) {
    active_wallet_address.text = walletAddress
  }

  override fun setAddressToClipBoard(walletAddress: String) {
    val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText(MyAddressActivity.KEY_ADDRESS, walletAddress)
    clipboard?.setPrimaryClip(clip)

    view?.let {
      Snackbar.make(it, R.string.wallets_address_copied_body, Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  override fun showQrCodeView() {
    context?.let { startActivityForResult(QrCodeActivity.newIntent(it), 12) }
  }

  override fun backPressed() = onBackPressedSubject!!

  override fun homeBackPressed() = activityView?.backPressed()

  override fun handleBackPress() {
    if (walletsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
      walletsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    } else {
      activityView?.navigateToTransactions()
    }
  }

  override fun openWalletVerificationScreen() {
    context?.let {
      val intent = VerificationActivity.newIntent(it)
          .apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
          }
      startActivity(intent)
    }
  }

  override fun showVerifiedWalletChip() {
    verified_wallet_chip.visibility = View.VISIBLE
  }

  override fun hideVerifiedWalletChip() {
    verified_wallet_chip.visibility = View.GONE
  }

  override fun showUnverifiedWalletChip() {
    unverified_wallet_layout.visibility = View.VISIBLE
  }

  override fun hideUnverifiedWalletChip() {
    unverified_wallet_layout.visibility = View.GONE
  }

  override fun showRequestedCodeWalletChip() {
    code_requested_layout.visibility = View.VISIBLE
  }

  override fun hideRequestedCodeWalletChip() {
    code_requested_layout.visibility = View.GONE
  }

  override fun disableVerifyWalletButton() {
    verify_wallet_button.isEnabled = false
  }

  override fun enableVerifyWalletButton() {
    verify_wallet_button.isEnabled = true
  }

  override fun enableInsertCodeButton() {
    insert_code_button.isEnabled = true
  }

  override fun disableInserCodeButton() {
    insert_code_button.isEnabled = false
  }

  override fun showCreatingAnimation() {
    showingAnimation = true
    activityView?.showCreatingAnimation()
  }

  override fun showWalletCreatedAnimation() {
    showingAnimation = false
    activityView?.showWalletCreatedAnimation()
  }

  override fun changeBottomSheetState() {
    if (walletsBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
      walletsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    } else if (walletsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
      walletsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    }
  }

  override fun dismissTooltip() {
    faded_background.visibility = View.GONE
    popup?.dismiss()
  }

  private fun setBackListener(view: View) {
    activityView?.disableBack()
    view.apply {
      isFocusableInTouchMode = true
      requestFocus()
      setOnKeyListener { _, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          if (popup != null && popup?.isShowing == true) {
            dismissTooltip()
            presenter.saveSeenToolTip()
          } else if (!showingAnimation) onBackPressedSubject?.onNext("")
        }
        true
      }
    }
  }

  private fun animateBackgroundFade() {
    walletsBottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        background_fade_animation?.progress = slideOffset
      }
    })
  }

  private fun setAlpha(view: View, alphaPercentage: Float) {
    view.alpha = 1 - alphaPercentage * 1.20f
  }
}
