package com.asfoundation.wallet.my_wallets.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentMyWalletsBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.ui.MyAddressActivity
import com.asfoundation.wallet.ui.balance.BalanceVerificationStatus
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.generateQrCode
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.util.*
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
    setListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshData(flushAsync = false)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    binding = null
  }

  private fun setListeners() {
    views.toolbar.actionButtonMore.setOnClickListener { navigateToMore() }
    views.toolbar.actionButtonNfts.setOnClickListener { navigator.navigateToNfts() }
  }

  override fun onStateChanged(state: MyWalletsState) {
    when (val asyncValue = state.walletInfoAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        showWalletInfoLoading()
        showBackupLoading()
      }
      is Async.Fail -> Unit
      is Async.Success -> asyncValue().run {
        showWalletInfo()
        showBackup()
      }
    }
    when (val asyncValue = state.walletVerifiedAsync) {
      Async.Uninitialized,
      is Async.Loading -> showVerifyLoading()
      is Async.Fail -> Unit
      is Async.Success -> asyncValue().run {
        when (status) {
          BalanceVerificationStatus.VERIFIED -> showVerified(false)
          BalanceVerificationStatus.UNVERIFIED -> showUnverified(false)
          BalanceVerificationStatus.CODE_REQUESTED -> showUnverifiedInsertCode(false)
          BalanceVerificationStatus.NO_NETWORK, BalanceVerificationStatus.ERROR -> {
            when (cachedStatus) {
              BalanceVerificationStatus.VERIFIED -> showVerified(true)
              BalanceVerificationStatus.UNVERIFIED -> showUnverified(true)
              BalanceVerificationStatus.CODE_REQUESTED -> showUnverifiedInsertCode(true)
              else -> showUnverified(true)
            }
          }
          else -> {
            when (cachedStatus) {
              BalanceVerificationStatus.VERIFIED -> showVerified(false)
              BalanceVerificationStatus.UNVERIFIED -> showUnverified(false)
              BalanceVerificationStatus.CODE_REQUESTED -> showUnverifiedInsertCode(false)
              BalanceVerificationStatus.VERIFYING -> showVerifying()
              else -> showUnverified(true)
            }
          }
        }
      }
    }
  }

  private fun showWalletInfoLoading() {
    views.myWalletsContent.qrImage.setImageResource(R.drawable.background_card)
    views.myWalletsContent.qrImage.isEnabled = false

    views.myWalletsContent.walletNameSkeleton.visibility = View.VISIBLE
    views.myWalletsContent.walletNameSkeleton.playAnimation()
    views.myWalletsContent.walletNameTextView.visibility = View.GONE

    views.myWalletsContent.walletAddressSkeleton.visibility = View.VISIBLE
    views.myWalletsContent.walletAddressSkeleton.playAnimation()
    views.myWalletsContent.walletAddressTextView.visibility = View.GONE

    views.myWalletsContent.totalBalanceSkeleton.visibility = View.VISIBLE
    views.myWalletsContent.totalBalanceSkeleton.playAnimation()
    views.myWalletsContent.totalBalanceTextView.visibility = View.GONE

    views.myWalletsContent.actionButtonEditName.isEnabled = false
    views.myWalletsContent.actionButtonShareAddress.isEnabled = false
    views.myWalletsContent.actionButtonCopyAddress.isEnabled = false
  }

  private fun WalletInfo.showWalletInfo() {
    try {
      val logo = ResourcesCompat.getDrawable(resources, R.drawable.ic_appc_token, null)
      val mergedQrCode = wallet.generateQrCode(requireActivity().windowManager, logo!!)
      views.myWalletsContent.qrImage.setImageBitmap(mergedQrCode)
    } catch (e: Exception) {
      Snackbar.make(
        views.myWalletsContent.qrImage,
        getString(R.string.error_fail_generate_qr),
        Snackbar.LENGTH_SHORT
      ).show()
    }
    views.myWalletsContent.qrImage.isEnabled = true
    views.myWalletsContent.qrImage.setOnClickListener {
      navigator.navigateToQrCode(views.myWalletsContent.qrImage)
    }

    views.myWalletsContent.sendButton.setOnClickListener { navigator.navigateToSend() }
    views.myWalletsContent.receiveButton.setOnClickListener {
      navigator.navigateToReceive(Wallet(wallet))
    }

    val address = wallet.replaceRange(IntRange(6, wallet.length - 5), " ··· ")

    views.myWalletsContent.walletNameSkeleton.visibility = View.GONE
    views.myWalletsContent.walletNameSkeleton.playAnimation()
    views.myWalletsContent.walletNameTextView.text = name
    views.myWalletsContent.walletNameTextView.visibility = View.VISIBLE

    views.myWalletsContent.walletAddressSkeleton.visibility = View.GONE
    views.myWalletsContent.walletAddressSkeleton.playAnimation()
    views.myWalletsContent.walletAddressTextView.text = address
    views.myWalletsContent.walletAddressTextView.visibility = View.VISIBLE

    val overallBalance = walletBalance.overallFiat.getFiatBalanceText()
    if (overallBalance != "-1") {
      views.myWalletsContent.totalBalanceSkeleton.visibility = View.GONE
      views.myWalletsContent.totalBalanceSkeleton.cancelAnimation()
      views.myWalletsContent.totalBalanceTextView.text = overallBalance
      views.myWalletsContent.totalBalanceTextView.visibility = View.VISIBLE
    }

    views.myWalletsContent.balanceButton.setOnClickListener {
      navigator.navigateToBalanceDetails(
        walletBalance.overallFiat.getFiatBalanceText(),
        walletBalance.appcBalance.getTokenValueText(WalletCurrency.APPCOINS),
        walletBalance.creditsBalance.getTokenValueText(WalletCurrency.CREDITS),
        walletBalance.ethBalance.getTokenValueText(WalletCurrency.ETHEREUM)
      )
    }

    views.myWalletsContent.actionButtonEditName.isEnabled = true
    views.myWalletsContent.actionButtonEditName.setOnClickListener {
      navigator.navigateToName(wallet, name)
    }

    views.myWalletsContent.actionButtonShareAddress.isEnabled = true
    views.myWalletsContent.actionButtonShareAddress.setOnClickListener {
      showShare(wallet)
    }

    views.myWalletsContent.actionButtonCopyAddress.isEnabled = true
    views.myWalletsContent.actionButtonCopyAddress.setOnClickListener {
      setAddressToClipBoard(wallet)
    }
  }

  private fun showBackupLoading() {
    views.myWalletsContent.backupLoading.visibility = View.VISIBLE
    views.myWalletsContent.backupAlertIcon.visibility = View.GONE
    views.myWalletsContent.backupWalletTitle.visibility = View.GONE
    views.myWalletsContent.backupWalletText.visibility = View.GONE
    views.myWalletsContent.backupButton.visibility = View.GONE
  }

  private fun WalletInfo.showBackup() {
    val imageRes = if (hasBackup) R.drawable.ic_check_circle else R.drawable.ic_alert_circle
    val colorRes = if (hasBackup) R.color.styleguide_white else R.color.styleguide_pink
    val titleRes = if (hasBackup) {
      R.string.backup_confirmation_no_share_title
    } else {
      R.string.my_wallets_action_backup_wallet
    }
    // If the date is 0 or 1, then either wallet was not backup yet or the backup date is unknown
    val text = if (backupDate > 1) {
      getString(
        R.string.mywallet_backed_up_date,
        DateFormat.format("dd/MM/yyyy", Date(backupDate)).toString()
      )
    } else {
      getString(R.string.backup_wallet_tooltip)
    }
    val buttonTextRes = if (hasBackup) {
      R.string.mywallet_backup_again_button
    } else {
      R.string.my_wallets_action_backup_wallet
    }
    views.myWalletsContent.backupLoading.visibility = View.GONE
    views.myWalletsContent.backupAlertIcon.visibility = View.VISIBLE
    views.myWalletsContent.backupAlertIcon.setImageResource(imageRes)
    views.myWalletsContent.backupWalletTitle.visibility = View.VISIBLE
    views.myWalletsContent.backupWalletTitle.setText(titleRes)
    views.myWalletsContent.backupWalletText.visibility = View.VISIBLE
    views.myWalletsContent.backupWalletText.text = text
    views.myWalletsContent.backupButton.visibility = View.VISIBLE
    views.myWalletsContent.backupButton.setText(getString(buttonTextRes))
    views.myWalletsContent.backupButton.setColor(ContextCompat.getColor(requireContext(), colorRes))
    views.myWalletsContent.backupButton.setOnClickListener {
      navigator.navigateToBackupWallet(wallet)
      walletsEventSender.sendCreateBackupEvent(null, WalletsAnalytics.MY_WALLETS, null)
    }
  }

  private fun showVerifyLoading() {
    views.myWalletsContent.verifyLoading.visibility = View.VISIBLE
    views.myWalletsContent.verifyLoadingText.visibility = View.GONE
    views.myWalletsContent.verifyAlertIcon.visibility = View.GONE
    views.myWalletsContent.verifyWalletTitle.visibility = View.GONE
    views.myWalletsContent.verifyWalletText.visibility = View.GONE
    views.myWalletsContent.verifyButton.visibility = View.GONE
  }

  private fun showVerified(disableButton: Boolean) {
    views.myWalletsContent.verifyLoading.visibility = View.GONE
    views.myWalletsContent.verifyLoadingText.visibility = View.GONE
    views.myWalletsContent.verifyAlertIcon.visibility = View.VISIBLE
    views.myWalletsContent.verifyAlertIcon.setImageResource(R.drawable.ic_check_circle)
    views.myWalletsContent.verifyWalletTitle.visibility = View.VISIBLE
    views.myWalletsContent.verifyWalletTitle.setText(R.string.verification_settings_verified_title)
    views.myWalletsContent.verifyWalletText.visibility = View.VISIBLE
    views.myWalletsContent.verifyWalletText.setText(R.string.mywallet_unverified_body)
    views.myWalletsContent.verifyButton.visibility = if (!disableButton) View.VISIBLE else View.GONE
    views.myWalletsContent.verifyButton.setText(getString(R.string.mywallet_verify_payment_method_button))
    views.myWalletsContent.verifyButton.setColor(
      ContextCompat.getColor(
        requireContext(),
        R.color.styleguide_white
      )
    )
    views.myWalletsContent.verifyButton.setOnClickListener {
      navigator.navigateToVerifyPicker()
    }
  }

  private fun showUnverified(disableButton: Boolean) {
    views.myWalletsContent.verifyLoading.visibility = View.GONE
    views.myWalletsContent.verifyLoadingText.visibility = View.GONE
    views.myWalletsContent.verifyAlertIcon.visibility = View.VISIBLE
    views.myWalletsContent.verifyAlertIcon.setImageResource(R.drawable.ic_alert_circle)
    views.myWalletsContent.verifyWalletTitle.visibility = View.VISIBLE
    views.myWalletsContent.verifyWalletTitle.setText(R.string.mywallet_unverified_title)
    views.myWalletsContent.verifyWalletText.visibility = View.VISIBLE
    views.myWalletsContent.verifyWalletText.setText(R.string.mywallet_unverified_body)
    views.myWalletsContent.verifyButton.visibility = if (!disableButton) View.VISIBLE else View.GONE
    views.myWalletsContent.verifyButton.setText(getString(R.string.referral_view_verify_button))
    views.myWalletsContent.verifyButton.setColor(
      ContextCompat.getColor(
        requireContext(),
        R.color.styleguide_pink
      )
    )
    views.myWalletsContent.verifyButton.setOnClickListener {
      navigator.navigateToVerifyPicker()
    }
  }

  private fun showUnverifiedInsertCode(disableButton: Boolean) {
    views.myWalletsContent.verifyLoading.visibility = View.GONE
    views.myWalletsContent.verifyLoadingText.visibility = View.GONE
    views.myWalletsContent.verifyAlertIcon.visibility = View.VISIBLE
    views.myWalletsContent.verifyAlertIcon.setImageResource(R.drawable.ic_alert_circle)
    views.myWalletsContent.verifyWalletTitle.visibility = View.VISIBLE
    views.myWalletsContent.verifyWalletTitle.setText(R.string.card_verification_wallets_one_step_title)
    views.myWalletsContent.verifyWalletText.visibility = View.VISIBLE
    views.myWalletsContent.verifyWalletText.setText(R.string.card_verification_wallets_one_step_body)
    views.myWalletsContent.verifyButton.visibility = if (!disableButton) View.VISIBLE else View.GONE
    views.myWalletsContent.verifyButton.setText(getString(R.string.card_verification_wallets_insert_bode_button))
    views.myWalletsContent.verifyButton.setColor(
      ContextCompat.getColor(
        requireContext(),
        R.color.styleguide_pink
      )
    )
    views.myWalletsContent.verifyButton.setOnClickListener {
      navigator.navigateToVerifyCreditCard()
    }
  }

  private fun showVerifying() {
    views.myWalletsContent.verifyLoading.visibility = View.VISIBLE
    views.myWalletsContent.verifyLoadingText.visibility = View.VISIBLE
    views.myWalletsContent.verifyAlertIcon.visibility = View.GONE
    views.myWalletsContent.verifyWalletTitle.visibility = View.GONE
    views.myWalletsContent.verifyWalletText.visibility = View.GONE
    views.myWalletsContent.verifyButton.visibility = View.GONE
  }

  override fun onSideEffect(sideEffect: MyWalletsSideEffect) = Unit

  private fun FiatValue.getFiatBalanceText(): String =
    if (amount.compareTo(BigDecimal("-1")) == 1) {
      symbol + formatter.formatCurrency(amount)
    } else {
      "-1"
    }

  private fun TokenBalance.getTokenValueText(tokenCurrency: WalletCurrency): String =
    "${
      if (token.amount.compareTo(BigDecimal("-1")) == 1) {
        formatter.formatCurrency(token.amount, tokenCurrency)
      } else {
        "-1"
      }
    } ${token.symbol}"

  private fun navigateToMore() {
    viewModel.state.walletInfoAsync()?.run {
      navigator.navigateToMore(
        wallet,
        walletBalance.overallFiat.getFiatBalanceText(),
        walletBalance.appcBalance.getTokenValueText(WalletCurrency.APPCOINS),
        walletBalance.creditsBalance.getTokenValueText(WalletCurrency.CREDITS),
        walletBalance.ethBalance.getTokenValueText(WalletCurrency.ETHEREUM)
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