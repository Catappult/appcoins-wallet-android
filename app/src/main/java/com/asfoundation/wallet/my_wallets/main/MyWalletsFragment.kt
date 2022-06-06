package com.asfoundation.wallet.my_wallets.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentMyWalletsBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.my_wallets.main.list.WalletsController
import com.asfoundation.wallet.my_wallets.main.list.WalletsListEvent
import com.asfoundation.wallet.ui.MyAddressActivity
import com.asfoundation.wallet.ui.balance.BalanceVerificationStatus
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.getDrawableURI
import com.asfoundation.wallet.util.safeLet
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class MyWalletsFragment : BasePageViewFragment(),
  SingleStateFragment<MyWalletsState, MyWalletsSideEffect> {

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: MyWalletsNavigator

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  private val viewModel: MyWalletsViewModel by viewModels()

  private var binding: FragmentMyWalletsBinding? = null
  private val views get() = binding!!

  private lateinit var walletsController: WalletsController
  private val epoxyVisibilityTracker = EpoxyVisibilityTracker()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentMyWalletsBinding.inflate(inflater, container, false)
    return views.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initializeView()
    setListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshData(flushAsync = false)
  }

  override fun onDestroyView() {
    epoxyVisibilityTracker.detach(views.otherWalletsRecyclerView)
    super.onDestroyView()
    binding = null
  }

  private fun initializeView() {
    walletsController = WalletsController()
    walletsController.walletClickListener = { click ->
      when (click) {
        WalletsListEvent.CreateNewWalletClick -> navigator.navigateToCreateNewWallet()
        is WalletsListEvent.OtherWalletClick -> navigator.navigateToChangeActiveWallet(click.walletBalance)
        is WalletsListEvent.CopyWalletClick -> setAddressToClipBoard(click.walletAddress)
        is WalletsListEvent.ShareWalletClick -> showShare(click.walletAddress)
        WalletsListEvent.BackupClick -> {
          viewModel.state.walletInfoAsync()
            ?.let { walletInfo ->
              navigator.navigateToBackupWallet(walletInfo.wallet)
              walletsEventSender.sendCreateBackupEvent(null, WalletsAnalytics.MY_WALLETS, null)
            }
        }
        WalletsListEvent.VerifyWalletClick -> navigator.navigateToVerifyPicker()
        WalletsListEvent.VerifyInsertCodeClick -> navigator.navigateToVerifyCreditCard()
        is WalletsListEvent.QrCodeClick -> navigator.navigateToQrCode(click.view)
        is WalletsListEvent.TokenClick -> {
          when (click.token) {
            WalletsListEvent.TokenClick.Token.APPC -> {
              navigateToTokenInfo(
                R.string.appc_token_name,
                R.string.p2p_send_currency_appc,
                R.drawable.ic_appc,
                R.string.balance_appcoins_body,
                false
              )
            }
            WalletsListEvent.TokenClick.Token.APPC_C -> {
              navigateToTokenInfo(
                R.string.appc_credits_token_name,
                R.string.p2p_send_currency_appc_c,
                R.drawable.ic_appc_c_token,
                R.string.balance_appccreditos_body,
                true
              )
            }
            WalletsListEvent.TokenClick.Token.ETH -> {
              navigateToTokenInfo(
                R.string.ethereum_token_name,
                R.string.p2p_send_currency_eth,
                R.drawable.ic_eth_token,
                R.string.balance_ethereum_body,
                false
              )
            }
          }
        }
        is WalletsListEvent.ChangedBalanceVisibility -> {
          if (click.balanceVisible) {
            binding?.titleSwitcher?.setText(getString(R.string.wallets_active_wallet_title))
          } else {
            viewModel.state.walletInfoAsync()
              ?.let { balance ->
                binding?.titleSwitcher?.setText(getFiatBalanceText(balance.walletBalance.overallFiat))
              }
          }
        }
      }
    }
    epoxyVisibilityTracker.attach(views.otherWalletsRecyclerView)
    views.otherWalletsRecyclerView.setController(walletsController)
  }

  private fun navigateToTokenInfo(
    tokenNameRes: Int,
    tokenSymbolRes: Int,
    tokenImageRes: Int,
    tokenDescriptionRes: Int,
    showTopUp: Boolean
  ) {
    val title = "${getString(tokenNameRes)} (${getString(tokenSymbolRes)})"
    val image = requireContext().getDrawableURI(tokenImageRes)
    val description = getString(tokenDescriptionRes)
    navigator.navigateToTokenInfo(title, image, description, showTopUp)
  }

  private fun setListeners() {
    views.actionButtonMore.setOnClickListener { navigateToMore() }
    views.actionButtonNfts.setOnClickListener { navigator.navigateToNfts() }
  }

  override fun onStateChanged(state: MyWalletsState) =
    walletsController.setData(state.walletsAsync, state.walletVerifiedAsync, state.walletInfoAsync)

  override fun onSideEffect(sideEffect: MyWalletsSideEffect) = Unit

  private fun getFiatBalanceText(balance: FiatValue): String {
    var overallBalance = "-1"
    if (balance.amount.compareTo(BigDecimal("-1")) == 1) {
      overallBalance = formatter.formatCurrency(balance.amount)
    }
    if (overallBalance != "-1") {
      return balance.symbol + overallBalance
    }
    return overallBalance
  }

  private fun getTokenValueText(balance: TokenBalance, tokenCurrency: WalletCurrency): String {
    var tokenBalance = "-1"
    if (balance.token.amount.compareTo(BigDecimal("-1")) == 1) {
      tokenBalance = formatter.formatCurrency(balance.token.amount, tokenCurrency)
    }
    return tokenBalance
  }

  private fun navigateToMore() {
    safeLet(
      viewModel.state.walletInfoAsync(),
      viewModel.state.walletsAsync(),
      viewModel.state.walletVerifiedAsync()
    ) { walletInfo, walletsModel, verifyModel ->
      val verifyStatus = verifyModel.status ?: verifyModel.cachedStatus
      val verified = verifyStatus == BalanceVerificationStatus.VERIFIED
      val overallFiatValue = getFiatBalanceText(walletInfo.walletBalance.overallFiat)
      val appcoinsValue = "${
        getTokenValueText(walletInfo.walletBalance.appcBalance, WalletCurrency.APPCOINS)
      } ${walletInfo.walletBalance.appcBalance.token.symbol}"
      val creditsValue = "${
        getTokenValueText(walletInfo.walletBalance.creditsBalance, WalletCurrency.CREDITS)
      } ${walletInfo.walletBalance.creditsBalance.token.symbol}"
      val ethValue = "${
        getTokenValueText(walletInfo.walletBalance.ethBalance, WalletCurrency.ETHEREUM)
      } ${walletInfo.walletBalance.ethBalance.token.symbol}"
      navigator.navigateToMore(
        walletInfo.wallet,
        overallFiatValue,
        appcoinsValue,
        creditsValue,
        ethValue,
        verified,
        walletsModel.wallets.size > 1
      )
    }
  }

  private fun setAddressToClipBoard(walletAddress: String) {
    val clipboard =
      requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText(MyAddressActivity.KEY_ADDRESS, walletAddress)
    clipboard?.setPrimaryClip(clip)
    val bottomNavView: BottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)!!

    Snackbar.make(bottomNavView, R.string.wallets_address_copied_body, Snackbar.LENGTH_SHORT)
      .apply { anchorView = bottomNavView }
      .show()
  }

  fun showShare(walletAddress: String) = ShareCompat.IntentBuilder(requireActivity())
    .setText(walletAddress)
    .setType("text/plain")
    .setChooserTitle(resources.getString(R.string.share_via))
    .startChooser()
}