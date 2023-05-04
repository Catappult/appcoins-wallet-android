package com.asfoundation.wallet.onboarding_new_payment.local_payments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.arch.data.Async
import com.asf.wallet.R
import com.asf.wallet.databinding.LocalPaymentLayoutBinding
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingLocalPaymentFragment : BasePageViewFragment(),
    SingleStateFragment<OnboardingLocalPaymentState, OnboardingLocalPaymentSideEffect> {

    private val viewModel: OnboardingLocalPaymentViewModel by viewModels()
    private val binding by lazy { LocalPaymentLayoutBinding.bind(requireView()) }
    private var errorMessage = R.string.activity_iab_error_message
    lateinit var args: OnboardingLocalPaymentFragmentArgs

    @Inject
    lateinit var formatter: CurrencyFormatUtils

    @Inject
    lateinit var navigator: OnboardingLocalPaymentNavigator

    private lateinit var webViewLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        return LocalPaymentLayoutBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args = OnboardingLocalPaymentFragmentArgs.fromBundle(requireArguments())
        createResultLauncher()
        clickListeners()
        viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    }

    override fun onStateChanged(state: OnboardingLocalPaymentState) {
        when (state.transaction) {
            Async.Uninitialized,
            is Async.Loading -> {
                showProcessingLoading()
            }
            is Async.Success -> {
                state.transaction.value?.url?.let {
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


    override fun onSideEffect(sideEffect: OnboardingLocalPaymentSideEffect) {
        when (sideEffect) {
            is OnboardingLocalPaymentSideEffect.NavigateToWebView -> {
                hideLoading()
                navigator.navigateToWebView(
                    sideEffect.uri,
                    webViewLauncher
                )

            }
            OnboardingLocalPaymentSideEffect.NavigateBackToPaymentMethods -> navigator.navigateBackToPaymentMethods()
            is OnboardingLocalPaymentSideEffect.ShowError -> showError(message = sideEffect.message)
            OnboardingLocalPaymentSideEffect.ShowLoading -> showProcessingLoading()
            OnboardingLocalPaymentSideEffect.ShowSuccess -> showCompletedPayment()
        }
    }

    private fun clickListeners() {
        binding.errorView.errorDismiss.setOnClickListener {
            viewModel.handleBackButton()
        }
    }

    private fun showProcessingLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorView.root.visibility = View.GONE
        binding.pendingUserPaymentView.root.visibility = View.GONE
        binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.cancelAnimation()
    }

    fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.errorView.root.visibility = View.GONE
        binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.cancelAnimation()
        binding.pendingUserPaymentView.root.visibility = View.GONE
        binding.completePaymentView.visibility = View.GONE
    }

    private fun showCompletedPayment() {
        binding.progressBar.visibility = View.GONE
        binding.errorView.root.visibility = View.GONE
        binding.pendingUserPaymentView.root.visibility = View.GONE
        binding.completePaymentView.visibility = View.VISIBLE
        binding.fragmentIabTransactionCompleted.iabActivityTransactionCompleted.visibility = View.VISIBLE
        binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.playAnimation()
        binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
    }

    fun showError(message: Int?) {
        binding.errorView.genericErrorLayout.errorMessage.text = getString(R.string.ok)
        message?.let { errorMessage = it }
        binding.errorView.genericErrorLayout.errorMessage.text = getString(message ?: errorMessage)
        binding.pendingUserPaymentView.root.visibility = View.GONE
        binding.completePaymentView.visibility = View.GONE
        binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.cancelAnimation()
        binding.progressBar.visibility = View.GONE
        binding.errorView.root.visibility = View.VISIBLE
    }

    fun close() {
        binding.progressBar.visibility = View.GONE
        binding.errorView.root.visibility = View.GONE
        binding.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.cancelAnimation()
        binding.pendingUserPaymentView.root.visibility = View.GONE
        binding.completePaymentView.visibility = View.GONE
    }
    fun getAnimationDuration() = binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.duration

    fun popView(bundle: Bundle, paymentId: String) {
        bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY, paymentId)
    }

}
