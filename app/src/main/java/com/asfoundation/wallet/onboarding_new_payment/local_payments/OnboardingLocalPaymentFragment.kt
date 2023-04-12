package com.asfoundation.wallet.onboarding_new_payment.local_payments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asf.wallet.R
import com.asf.wallet.databinding.LocalPaymentLayoutBinding
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentView
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_iab.*
import kotlinx.android.synthetic.main.activity_token_details.view.*
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.view.*
import kotlinx.android.synthetic.main.pending_user_payment_view.view.*
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingLocalPaymentFragment : BasePageViewFragment(),
    SingleStateFragment<OnboardingLocalPaymentState, OnboardingLocalPaymentSideEffect> {

    private val viewModel: OnboardingLocalPaymentViewModel by viewModels()
    private val views by viewBinding(LocalPaymentLayoutBinding::bind)
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
        showProcessingLoading()
        clickListeners()
        viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    }

    private fun clickListeners() {
    }

    override fun onStateChanged(state: OnboardingLocalPaymentState) {
        TODO("Not yet implemented")
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
                navigator.navigateToWebView(
                    sideEffect.uri,
                    webViewLauncher
                )

            }
            OnboardingLocalPaymentSideEffect.NavigateBackToPaymentMethods -> navigator.navigateBackToPaymentMethods()
            OnboardingLocalPaymentSideEffect.ShowError -> showError()
            OnboardingLocalPaymentSideEffect.ShowLoading -> showProcessingLoading()
            OnboardingLocalPaymentSideEffect.ShowSuccess -> showCompletedPayment()
            is OnboardingLocalPaymentSideEffect.HandleWebViewResult -> TODO()
            OnboardingLocalPaymentSideEffect.ShowVerification -> showVerification()
            OnboardingLocalPaymentSideEffect.ShowCompletablePayment -> showCompletedPayment()
        }
    }

    fun showProcessingLoading() {
        LocalPaymentView.ViewState.LOADING
        views.progressBar.visibility = View.VISIBLE
        views.errorView.root.visibility = View.GONE
        views.pendingUserPaymentView.root.visibility = View.GONE
        views.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        views.completePaymentView.lottie_transaction_success.cancelAnimation()
    }

    fun hideLoading() {
        views.progressBar.visibility = View.GONE
        views.errorView.root.visibility = View.GONE
        views.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        views.completePaymentView.lottie_transaction_success.cancelAnimation()
        views.pendingUserPaymentView.root.visibility = View.GONE
        views.completePaymentView.root.visibility = View.GONE
    }

    fun showCompletedPayment() {
        LocalPaymentView.ViewState.COMPLETED
        views.progressBar.visibility = View.GONE
        views.errorView.root.visibility = View.GONE
        views.pendingUserPaymentView.root.visibility = View.GONE
        views.completePaymentView.root.visibility = View.VISIBLE
        views.completePaymentView.iab_activity_transaction_completed.visibility = View.VISIBLE
        views.completePaymentView.lottie_transaction_success.playAnimation()
        views.completePaymentView.in_progress_animation.cancelAnimation()
    }

    fun showPendingUserPayment(
        paymentMethodLabel: String?, paymentMethodIcon: Bitmap,
        applicationIcon: Bitmap
    ) {
        views.errorView.root.visibility = View.GONE
        views.completePaymentView.root.visibility = View.GONE
        views.progressBar.visibility = View.GONE
        views.pendingUserPaymentView.root.visibility = View.VISIBLE

        val placeholder = getString(R.string.async_steps_1_no_notification)
        val stepOneText = String.format(placeholder, paymentMethodLabel)

        views.pendingUserPaymentView.stepOneDesc.text = stepOneText

        views.pendingUserPaymentView.inProgressAnimation.updateBitmap("image_0", paymentMethodIcon)
        views.pendingUserPaymentView.inProgressAnimation.updateBitmap("image_1", applicationIcon)

    }

    fun showVerification() {
        //get on View Model isWalletVerified
        val isWalletVerified = false
        fragment_container.visibility = View.GONE
        val intent = VerificationCreditCardActivity.newIntent(requireContext(), isWalletVerified)
            .apply { activity?.intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        startActivity(intent)
        activity?.setResult(Activity.RESULT_CANCELED)
        activity?.finish()
    }

    fun showError() {
        LocalPaymentView.ViewState.ERROR
        //error_message.text = getString(R.string.ok)
        //message?.let { errorMessage = it }
        //error_message.text = getString(message ?: errorMessage)
        views.pendingUserPaymentView.root.visibility = View.GONE
        views.completePaymentView.visibility = View.GONE
        views.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        views.completePaymentView.lottie_transaction_success.cancelAnimation()
        views.progressBar.visibility = View.GONE
        views.errorView.root.visibility = View.VISIBLE
    }

    fun close() {
        LocalPaymentView.ViewState.NONE
        views.progressBar.visibility = View.GONE
        views.errorView.root.visibility = View.GONE
        views.pendingUserPaymentView.inProgressAnimation.cancelAnimation()
        views.completePaymentView.lottie_transaction_success.cancelAnimation()
        views.pendingUserPaymentView.root.visibility = View.GONE
        views.completePaymentView.visibility = View.GONE

    }
    fun getAnimationDuration() =  views.completePaymentView.lottie_transaction_success.duration

    fun popView(bundle: Bundle, paymentId: String) {
        bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY, paymentId)

    }

    enum class ViewState {
        NONE, COMPLETED, PENDING_USER_PAYMENT, ERROR, LOADING
    }

}
