package com.asfoundation.wallet.ui.transact

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.appcoins.wallet.core.utils.jvm_common.C
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TokenInfo
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.transfers.TransferConfirmationActivity
import com.asfoundation.wallet.transfers.TransferFundsViewModel
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.wallet_blocked.WalletBlockedActivity
import io.reactivex.Completable
import java.math.BigDecimal
import javax.inject.Inject

class TransferFragmentNavigator @Inject constructor(
  private val fragmentManager: FragmentManager,
  private val fragment: Fragment,
) {

  companion object {
    const val TRANSACTION_CONFIRMATION_REQUEST_CODE = 12344
    const val BARCODE_READER_REQUEST_CODE = 1
  }

  fun openSuccessView(
    walletAddress: String,
    amount: BigDecimal,
    currency: String,
    mainNavController: NavController
  ) {
    val bundle = Bundle()
    bundle.putSerializable(AppcoinsCreditsTransferSuccessFragment.AMOUNT_SENT_KEY, amount)
    bundle.putString(AppcoinsCreditsTransferSuccessFragment.CURRENCY_KEY, currency)
    bundle.putString(AppcoinsCreditsTransferSuccessFragment.TO_ADDRESS_KEY, walletAddress)
    mainNavController.navigate(
      resId = R.id.action_navigate_to_success_transfer,
      args = bundle
    )
  }

  private fun openConfirmation(transactionBuilder: TransactionBuilder) {
    val intent = Intent(fragment.context, TransferConfirmationActivity::class.java).apply {
      putExtra(C.EXTRA_TRANSACTION_BUILDER, transactionBuilder)
    }
    fragment.startActivityForResult(intent, TRANSACTION_CONFIRMATION_REQUEST_CODE)
  }

  fun navigateBack() = fragment.requireActivity().onBackPressed()

  fun showWalletBlocked() {
    fragment.startActivityForResult(
      WalletBlockedActivity.newIntent(fragment.requireActivity()),
      IabActivity.BLOCKED_WARNING_REQUEST_CODE
    )
  }

  fun showQrCodeScreen() {
    val intent = Intent(fragment.requireActivity(), BarcodeCaptureActivity::class.java)
    fragment.startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
  }

  fun showLoading() {
    fragmentManager.beginTransaction()
      .add(android.R.id.content, LoadingFragment.newInstance(), LoadingFragment::class.java.name)
      .commit()
  }

  fun hideLoading() {
    val fragment = fragmentManager.findFragmentByTag(LoadingFragment::class.java.name)
    if (fragment != null) {
      fragmentManager.beginTransaction()
        .remove(fragment)
        .commit()
    }
  }
}
