package com.asfoundation.wallet.my_wallets.neww

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentMyWalletsBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import com.asfoundation.wallet.ui.balance.BalanceVerificationModel
import com.asfoundation.wallet.ui.balance.BalanceVerificationStatus
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.generateQrCode
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.qr_code_layout.*
import java.math.BigDecimal
import javax.inject.Inject

class NewMyWalletsFragment : BasePageViewFragment(),
    SingleStateFragment<MyWalletsState, MyWalletsSideEffect> {

  @Inject
  lateinit var viewModelFactory: MyWalletsViewModelFactory

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val viewModel: MyWalletsViewModel by viewModels { viewModelFactory }

  private val views by viewBinding(FragmentMyWalletsBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_my_wallets, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: MyWalletsState) {
    setWalletAddress(state.walletAsync)
    setVerified(state.walletVerifiedAsync)
    setBalance(state.balanceAsync)
  }

  override fun onSideEffect(sideEffect: MyWalletsSideEffect) {

  }

  private fun setWalletAddress(walletAsync: Async<WalletAddressModel>) {
    when (walletAsync) {
      is Async.Success -> {
        val walletAddressModel = walletAsync()
        views.walletAddressTextView.text = walletAddressModel.address
        setQrCode(walletAddressModel.address)
      }
      else -> Unit
    }

  }

  private fun setBalance(balanceAsync: Async<BalanceScreenModel>) {
    when (balanceAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        views.appccValueSkeleton.playAnimation()
        views.appccValueSkeleton.visibility = View.VISIBLE
        views.appcValueSkeleton.playAnimation()
        views.appcValueSkeleton.visibility = View.VISIBLE
        views.ethValueSkeleton.playAnimation()
        views.ethValueSkeleton.visibility = View.VISIBLE
        views.totalBalanceSkeleton.playAnimation()
        views.totalBalanceSkeleton.visibility = View.VISIBLE
      }
      is Async.Fail -> {
      }
      is Async.Success -> {
        val balanceScreenModel = balanceAsync()
        updateTokenValue(balanceScreenModel.appcBalance, WalletCurrency.APPCOINS)
        updateTokenValue(balanceScreenModel.creditsBalance, WalletCurrency.CREDITS)
        updateTokenValue(balanceScreenModel.ethBalance, WalletCurrency.ETHEREUM)
        updateOverallBalance(balanceScreenModel.overallFiat)
      }
    }
  }

  private fun updateTokenValue(balance: TokenBalance, tokenCurrency: WalletCurrency) {
    var tokenBalance = "-1"
    var fiatBalance = "-1"
    val fiatCurrency = balance.fiat.symbol
    if (balance.token.amount.compareTo(BigDecimal("-1")) == 1) {
      tokenBalance = formatter.formatCurrency(balance.token.amount, tokenCurrency)
      fiatBalance = formatter.formatCurrency(balance.fiat.amount)
    }
    val tokenBalanceValueText = "$tokenBalance ${tokenCurrency.symbol}"
    val tokenFiatValueText = "$fiatCurrency$fiatBalance"
    if (tokenBalance != "-1" && fiatBalance != "-1") {
      when (tokenCurrency) {
        WalletCurrency.CREDITS -> {
          views.appccValueSkeleton.visibility = View.GONE
          views.appccValueSkeleton.cancelAnimation()
          views.appccValue.text = tokenBalanceValueText
          views.appccValue.visibility = View.VISIBLE
        }
        WalletCurrency.APPCOINS -> {
          views.appcValueSkeleton.visibility = View.GONE
          views.appcValueSkeleton.cancelAnimation()
          views.appcValue.text = tokenBalanceValueText
          views.appcValue.visibility = View.VISIBLE
        }
        WalletCurrency.ETHEREUM -> {
          views.ethValueSkeleton.visibility = View.GONE
          views.ethValueSkeleton.cancelAnimation()
          views.ethValue.text = tokenBalanceValueText
          views.ethValue.visibility = View.VISIBLE
        }
        else -> return
      }
    }
  }

  private fun updateOverallBalance(balance: FiatValue) {
    var overallBalance = "-1"
    if (balance.amount.compareTo(BigDecimal("-1")) == 1) {
      overallBalance = formatter.formatCurrency(balance.amount)
    }

    if (overallBalance != "-1") {
      val balanceText = balance.symbol + overallBalance
      views.totalBalanceSkeleton.visibility = View.GONE
      views.totalBalanceSkeleton.cancelAnimation()
      views.totalBalanceTextView.text = balanceText
      views.totalBalanceTextView.visibility = View.VISIBLE
    }
  }

  private fun setVerified(walletVerifiedAsync: Async<BalanceVerificationModel>) {
    when (walletVerifiedAsync) {
      is Async.Success -> {
        val verifiedModel = walletVerifiedAsync()
        when (verifiedModel.status) {
          BalanceVerificationStatus.VERIFIED -> setVerifiedWallet(true)
          BalanceVerificationStatus.UNVERIFIED -> setVerifiedWallet(false)
          BalanceVerificationStatus.CODE_REQUESTED -> setShowVerifyInsertCode()
          BalanceVerificationStatus.NO_NETWORK,
          BalanceVerificationStatus.ERROR -> {
            // Set cached value
            when (verifiedModel.cachedStatus) {
              BalanceVerificationStatus.VERIFIED -> setVerifiedWallet(true)
              BalanceVerificationStatus.UNVERIFIED -> setVerifiedWallet(verified = false,
                  disableButton = true)
              BalanceVerificationStatus.CODE_REQUESTED -> setShowVerifyInsertCode(true)
              else -> setVerifiedWallet(verified = false, disableButton = true)
            }
          }
          null -> {
            // Set cached value
            when (verifiedModel.cachedStatus) {
              BalanceVerificationStatus.VERIFIED -> setVerifiedWallet(true)
              BalanceVerificationStatus.UNVERIFIED -> setVerifiedWallet(false)
              BalanceVerificationStatus.CODE_REQUESTED -> setShowVerifyInsertCode()
              else -> setVerifiedWallet(false)
            }
          }
        }
      }
      else -> Unit
    }
  }

  private fun setVerifiedWallet(verified: Boolean, disableButton: Boolean = false) {
    views.insertCodeWalletCardView.visibility = View.GONE
    if (verified) {
      views.verifyWalletCardView.visibility = View.GONE
      views.verifiedWalletText.visibility = View.VISIBLE
      views.verifiedWalletIcon.visibility = View.VISIBLE
    } else {
      views.verifyButton.isEnabled = !disableButton
      views.verifyWalletCardView.visibility = View.VISIBLE
      views.verifiedWalletText.visibility = View.GONE
      views.verifiedWalletIcon.visibility = View.GONE
    }
  }

  private fun setShowVerifyInsertCode(disableButton: Boolean = false) {
    views.verifyWalletCardView.visibility = View.GONE
    views.verifiedWalletText.visibility = View.GONE
    views.verifiedWalletIcon.visibility = View.GONE
    views.insertCodeWalletCardView.visibility = View.VISIBLE
    views.insertCodeButton.isEnabled = !disableButton
  }

  private fun setQrCode(walletAddress: String) {
    // Up screen brightness
//    val params = requireActivity().window.attributes.apply { screenBrightness = 1f }
//    requireActivity().window.attributes = params
//    requireActivity().window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)
    try {
      val logo = ResourcesCompat.getDrawable(resources, R.drawable.ic_appc_token, null)
      val mergedQrCode = walletAddress.generateQrCode(requireActivity().windowManager, logo!!)
      views.qrImage.setImageBitmap(mergedQrCode)
    } catch (e: Exception) {
      Snackbar.make(main_layout, getString(R.string.error_fail_generate_qr), Snackbar.LENGTH_SHORT)
          .show()
    }
  }
}