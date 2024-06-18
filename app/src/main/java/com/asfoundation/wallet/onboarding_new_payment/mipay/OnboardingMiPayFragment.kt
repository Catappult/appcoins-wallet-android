package com.asfoundation.wallet.onboarding_new_payment.mipay

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.RedirectUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.MipayLayoutBinding
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingMiPayFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingMiPayState, OnboardingMiPaySideEffect> {

  private val viewModel: OnboardingMiPayViewModel by viewModels()
  private val binding by lazy { MipayLayoutBinding.bind(requireView()) }
  private var errorMessage = R.string.activity_iab_error_message
  lateinit var args: OnboardingMiPayFragmentArgs

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: OnboardingMiPayNavigator

  private lateinit var webViewLauncher: ActivityResultLauncher<Intent>

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return MipayLayoutBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    args = OnboardingMiPayFragmentArgs.fromBundle(requireArguments())
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    createResultLauncher()
    clickListeners()
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    viewModel.getPaymentLink(
      RedirectUtils.getReturnUrl(requireContext())
    )

  }

  override fun onStateChanged(state: OnboardingMiPayState) {
    when (state.transaction) {
      Async.Uninitialized,
      is Async.Loading -> {
        showProcessingLoading()
      }

      is Async.Success -> {
        state.transaction.value?.redirectUrl?.let {
          navigator.navigateToWebView(
            it,
            webViewLauncher
          )
        }
      }

      is Async.Fail -> {
        showError(null)
      }
    }
  }

  private fun createResultLauncher() {
    webViewLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.handleWebViewResult(result)
      }
  }


  override fun onSideEffect(sideEffect: OnboardingMiPaySideEffect) {
    when (sideEffect) {
      is OnboardingMiPaySideEffect.NavigateToWebView -> {
        hideLoading()
        navigator.navigateToWebView(
          sideEffect.uri,
          webViewLauncher
        )
      }

      OnboardingMiPaySideEffect.NavigateBackToPaymentMethods -> navigator.navigateBackToPaymentMethods()
      is OnboardingMiPaySideEffect.ShowError -> showError(message = sideEffect.message)
      OnboardingMiPaySideEffect.ShowLoading -> showProcessingLoading()
      OnboardingMiPaySideEffect.ShowSuccess -> showCompletedPayment()
      is OnboardingMiPaySideEffect.NavigateBackToTheGame -> navigator.navigateBackToTheGame(
        sideEffect.domain
      )

      OnboardingMiPaySideEffect.NavigateToHome -> navigator.navigateToHome()
    }
  }

  private fun clickListeners() {
    binding.errorTryAgainMiPay.setOnClickListener {
      viewModel.handleBackButton()
    }
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
      navigator.navigateBack()
    }
    binding.onboardingSuccessMiPayButtons.backToGameButton.setOnClickListener {
      viewModel.handleBackToGameClick()
    }
    binding.onboardingSuccessMiPayButtons.exploreWalletButton.setOnClickListener {
      viewModel.handleExploreWalletClick()
    }
    binding.errorView.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport()
    }
    binding.errorView.layoutSupportLogo.setOnClickListener {
      viewModel.showSupport()
    }
  }

  private fun showProcessingLoading() {
    binding.progressBar.visibility = View.VISIBLE
    binding.errorView.root.visibility = View.GONE
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.fragmentIabTransactionCompleted.onboardingLottieTransactionSuccess.cancelAnimation()
  }

  fun hideLoading() {
    binding.progressBar.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.fragmentIabTransactionCompleted.onboardingLottieTransactionSuccess.cancelAnimation()
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.GONE
  }

  private fun showCompletedPayment() {
    binding.progressBar.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.VISIBLE
    binding.fragmentIabTransactionCompleted.onboardingActivityTransactionCompleted.visibility =
      View.VISIBLE
    binding.fragmentIabTransactionCompleted.onboardingLottieTransactionSuccess.playAnimation()
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.onboardingSuccessMiPayButtons.root.visibility = View.VISIBLE
  }

  fun showError(message: Int?) {
    message?.let { errorMessage = it }
    binding.errorView.errorMessage.text = getString(message ?: errorMessage)
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.GONE
    binding.onboardingSuccessMiPayButtons.root.visibility = View.GONE
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.fragmentIabTransactionCompleted.onboardingLottieTransactionSuccess.cancelAnimation()
    binding.progressBar.visibility = View.GONE
    binding.errorView.root.visibility = View.VISIBLE
    binding.errorTryAgainMiPay.visibility = View.VISIBLE
  }

  fun close() {
    binding.progressBar.visibility = View.GONE
    binding.errorView.root.visibility = View.GONE
    binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    binding.fragmentIabTransactionCompleted.onboardingLottieTransactionSuccess.cancelAnimation()
    binding.pendingUserPaymentView.root.visibility = View.GONE
    binding.completePaymentView.visibility = View.GONE
  }

  fun getAnimationDuration() =
    binding.fragmentIabTransactionCompleted.onboardingLottieTransactionSuccess.duration

  fun popView(bundle: Bundle, paymentId: String) {
    bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY, paymentId)
  }

}
