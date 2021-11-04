package com.asfoundation.wallet.verification.ui.paypal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentVerifyPaypalIntroBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.Error
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroModel
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import javax.inject.Inject

class VerificationPaypalFragment : BasePageViewFragment(),
    SingleStateFragment<VerificationPaypalIntroState, VerificationPaypalIntroSideEffect> {

  @Inject
  lateinit var viewModelFactory: VerificationPaypalViewModelFactory

  @Inject
  lateinit var navigator: VerificationPaypalNavigator

  private val viewModel: VerificationPaypalIntroViewModel by viewModels { viewModelFactory }

  private val views by viewBinding(FragmentVerifyPaypalIntroBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(navigator)
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
      is Async.Success -> showVerificationInfo()
      is Async.Fail -> setError(verificationInfoAsync.error)
    }
  }

  override fun onSideEffect(sideEffect: VerificationPaypalIntroSideEffect) {
    when (sideEffect) {
      is VerificationPaypalIntroSideEffect.NavigateToPaymentUrl -> {
        navigator.navigateToPayment(sideEffect.url) { result: ActivityResult ->
          val resultCode = result.resultCode
          val data = result.data
          if (resultCode == WebViewActivity.SUCCESS && data != null) {
            viewModel.successPayment()
          } else if (resultCode == WebViewActivity.FAIL) {
            viewModel.failPayment()
          }
        }
      }
    }
  }

  private fun setError(error: Error) {
    if (error is Error.ApiError.NetworkError)
      showNetworkError()
    else
      showGenericError()
  }

  private fun showSuccessValidation() {
    views.progressBar.visibility = View.GONE
    views.noNetwork.root.visibility = View.GONE
    views.genericError.root.visibility = View.GONE
    views.paypalGraphic.visibility = View.GONE
    views.verifyGraphic.visibility = View.GONE
    views.paypalVerifyDescription.visibility = View.GONE
    views.verifyNowButton.visibility = View.GONE
    views.successVerification.root.visibility = View.VISIBLE
  }

  private fun showLoading() {
    views.progressBar.visibility = View.VISIBLE
    views.noNetwork.root.visibility = View.GONE
    views.genericError.root.visibility = View.GONE
    views.paypalGraphic.visibility = View.GONE
    views.verifyGraphic.visibility = View.GONE
    views.paypalVerifyDescription.visibility = View.GONE
    views.verifyNowButton.visibility = View.GONE
    views.successVerification.root.visibility = View.GONE
  }

  private fun showVerificationInfo() {
    views.progressBar.visibility = View.GONE
    views.noNetwork.root.visibility = View.GONE
    views.genericError.root.visibility = View.GONE
    views.successVerification.root.visibility = View.GONE
    views.paypalGraphic.visibility = View.VISIBLE
    views.verifyGraphic.visibility = View.VISIBLE
    views.paypalVerifyDescription.visibility = View.VISIBLE
    views.verifyNowButton.visibility = View.VISIBLE
  }

  private fun showGenericError() {
    views.progressBar.visibility = View.GONE
    views.paypalGraphic.visibility = View.GONE
    views.verifyGraphic.visibility = View.GONE
    views.paypalVerifyDescription.visibility = View.GONE
    views.verifyNowButton.visibility = View.GONE
    views.noNetwork.root.visibility = View.GONE
    views.successVerification.root.visibility = View.GONE
    views.genericError.errorTitle.visibility = View.VISIBLE
    views.genericError.maybeLater.visibility = View.VISIBLE
    views.genericError.errorMessage.text = getString(R.string.unknown_error)
    views.genericError.root.visibility = View.VISIBLE
  }

  private fun showNetworkError() {
    views.progressBar.visibility = View.GONE
    views.paypalGraphic.visibility = View.GONE
    views.verifyGraphic.visibility = View.GONE
    views.paypalVerifyDescription.visibility = View.GONE
    views.verifyNowButton.visibility = View.GONE
    views.genericError.root.visibility = View.GONE
    views.successVerification.root.visibility = View.GONE
    views.noNetwork.root.visibility = View.VISIBLE
  }

  companion object {
    @JvmStatic
    fun newInstance() = VerificationPaypalFragment()
  }
}