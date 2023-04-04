package com.asfoundation.wallet.verification.ui.paypal

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentVerifyPaypalIntroBinding
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.arch.data.Error
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroModel
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VerificationPaypalFragment : BasePageViewFragment(),
  SingleStateFragment<VerificationPaypalIntroState, VerificationPaypalIntroSideEffect> {

  @Inject
  lateinit var viewModelFactory: VerificationPaypalViewModelFactory

  @Inject
  lateinit var navigator: VerificationPaypalNavigator

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val viewModel: VerificationPaypalViewModel by viewModels { viewModelFactory }

  private val views by viewBinding(FragmentVerifyPaypalIntroBinding::bind)

  private val paypalActivityLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
    val resultCode = result.resultCode
    val data = result.data
    if (resultCode == WebViewActivity.SUCCESS && data != null) {
      viewModel.successPayment()
    } else if (resultCode == WebViewActivity.FAIL) {
      viewModel.failPayment()
    } else if (resultCode == WebViewActivity.USER_CANCEL) {
      viewModel.cancelPayment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_verify_paypal_intro, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.verifyNowButton.setOnClickListener { viewModel.launchVerificationPayment() }
    views.successVerification.closeBtn.setOnClickListener { navigator.navigateBack() }
    views.genericError.maybeLater.setOnClickListener { navigator.navigateBack() }
    views.genericError.tryAgain.setOnClickListener { viewModel.tryAgain() }
    views.genericError.tryAgainAttempts.setOnClickListener { viewModel.tryAgain() }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: VerificationPaypalIntroState) {
    when (state.verificationSubmitAsync) {
      Async.Uninitialized -> setVerificationInfoAsync(state.verificationInfoAsync)
      is Async.Loading -> showLoading()
      is Async.Fail -> setError(state.verificationSubmitAsync.error)
      is Async.Success -> showSuccessValidation()
    }
  }

  private fun setVerificationInfoAsync(verificationInfoAsync: Async<VerificationIntroModel>) {
    when (verificationInfoAsync) {
      Async.Uninitialized,
      is Async.Loading -> showLoading()
      is Async.Success -> showVerificationInfo(verificationInfoAsync())
      is Async.Fail -> setError(verificationInfoAsync.error)
    }
  }

  override fun onSideEffect(sideEffect: VerificationPaypalIntroSideEffect) {
    when (sideEffect) {
      is VerificationPaypalIntroSideEffect.NavigateToPaymentUrl -> {
        navigator.navigateToPayment(sideEffect.url, paypalActivityLauncher)
      }
    }
  }

  private fun setError(error: Error) {
    if (error is Error.ApiError.NetworkError)
      showNetworkError()
    else if (error.throwable.message.equals(WebViewActivity.USER_CANCEL_THROWABLE))
      handleUserCancelError()
    else
      showGenericError()
  }

  private fun showSuccessValidation() {
    hideAll()
    views.successVerification.root.visibility = View.VISIBLE
  }

  private fun showLoading() {
    hideAll()
    views.progressBar.visibility = View.VISIBLE
  }

  private fun showVerificationInfo(verificationIntroModel: VerificationIntroModel) {
    val amount = formatter.formatCurrency(verificationIntroModel.verificationInfoModel.value,
        WalletCurrency.FIAT)
    views.paypalVerifyDescription.text = getString(R.string.verification_verify_paypal_description,
        "${verificationIntroModel.verificationInfoModel.symbol}$amount")
    hideAll()
    views.paypalGraphic.visibility = View.VISIBLE
    views.verifyGraphic.visibility = View.VISIBLE
    views.paypalVerifyDescription.visibility = View.VISIBLE
    views.verifyNowButton.visibility = View.VISIBLE
  }

  private fun showNetworkError() {
    hideAll()
    views.noNetwork.root.visibility = View.VISIBLE
  }

  private fun showGenericError() {
    hideAll()
    views.genericError.root.visibility = View.VISIBLE
    views.genericError.errorTitle.visibility = View.VISIBLE
    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      views.genericError.attemptsGroup.visibility = View.GONE
      views.genericError.maybeLater.visibility = View.GONE
      views.genericError.tryAgainAttempts.visibility = View.GONE
      views.genericError.tryAgain.visibility = View.VISIBLE
    } else {
      views.genericError.attemptsGroup.visibility = View.VISIBLE
      views.genericError.maybeLater.visibility = View.VISIBLE
      views.genericError.tryAgainAttempts.visibility = View.VISIBLE
      views.genericError.tryAgain.visibility = View.GONE
    }
    views.genericError.errorMessage.text = getString(R.string.unknown_error)
  }

  private fun handleUserCancelError() {
    hideAll()
    navigator.navigateBack()
  }

  private fun hideAll() {
    views.successVerification.root.visibility = View.GONE
    views.paypalGraphic.visibility = View.GONE
    views.verifyGraphic.visibility = View.GONE
    views.paypalVerifyDescription.visibility = View.GONE
    views.verifyNowButton.visibility = View.GONE
    views.progressBar.visibility = View.GONE
    views.genericError.root.visibility = View.GONE
    views.noNetwork.root.visibility = View.GONE
  }

  companion object {
    @JvmStatic
    fun newInstance() = VerificationPaypalFragment()
  }
}