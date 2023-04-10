package com.asfoundation.wallet.onboarding_new_payment.local_payments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asf.wallet.R
import com.asf.wallet.databinding.LocalPaymentLayoutBinding
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.onboarding_new_payment.payment_result.*
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.localpayments.LocalPaymentView
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.view.*
import kotlinx.android.synthetic.main.local_payment_layout.*
import kotlinx.android.synthetic.main.pending_user_payment_view.*
import kotlinx.android.synthetic.main.pending_user_payment_view.view.*
import kotlinx.android.synthetic.main.support_error_layout.*
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingLocalPaymentFragment : BasePageViewFragment(),
    SingleStateFragment<OnboardingLocalPaymentState, OnboardingLocalPaymentSideEffect> {

    private val viewModel: OnboardingLocalPaymentViewModel by viewModels()
    private val views by viewBinding(LocalPaymentLayoutBinding::bind)
    private var errorMessage = R.string.activity_iab_error_message
    lateinit var args: OnboardingPaymentResultFragmentArgs

    @Inject
    lateinit var servicesErrorCodeMapper: ServicesErrorCodeMapper

    @Inject
    lateinit var adyenErrorCodeMapper: AdyenErrorCodeMapper

    @Inject
    lateinit var formatter: CurrencyFormatUtils

    @Inject
    lateinit var navigator: OnboardingPaymentResultNavigator

    override fun onCreateView(
        inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        return LocalPaymentLayoutBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args = OnboardingPaymentResultFragmentArgs.fromBundle(requireArguments())
        showProcessingLoading()
        clickListeners()
        viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    }

    private fun clickListeners() {
    }

    override fun onStateChanged(state: OnboardingLocalPaymentState) {

    }

    override fun onSideEffect(sideEffect: OnboardingLocalPaymentSideEffect) {
    }

    fun showProcessingLoading() {
        LocalPaymentView.ViewState.LOADING
        progress_bar.visibility = View.VISIBLE
        error_view.visibility = View.GONE
        pending_user_payment_view.visibility = View.GONE
        pending_user_payment_view.in_progress_animation.cancelAnimation()
        complete_payment_view.lottie_transaction_success.cancelAnimation()
    }

    fun hideLoading() {
        progress_bar.visibility = View.GONE
        error_view.visibility = View.GONE
        pending_user_payment_view.in_progress_animation.cancelAnimation()
        complete_payment_view.lottie_transaction_success.cancelAnimation()
        pending_user_payment_view.visibility = View.GONE
        complete_payment_view.visibility = View.GONE
    }

     fun showCompletedPayment() {
         LocalPaymentView.ViewState.COMPLETED
        progress_bar.visibility = View.GONE
        error_view.visibility = View.GONE
        pending_user_payment_view.visibility = View.GONE
        complete_payment_view.visibility = View.VISIBLE
        complete_payment_view.iab_activity_transaction_completed.visibility = View.VISIBLE
        complete_payment_view.lottie_transaction_success.playAnimation()
        pending_user_payment_view.in_progress_animation.cancelAnimation()
    }

     fun showPendingUserPayment(paymentMethodLabel: String?, paymentMethodIcon: Bitmap,
                                        applicationIcon: Bitmap
    ) {
        error_view.visibility = View.GONE
        complete_payment_view.visibility = View.GONE
        progress_bar.visibility = View.GONE
        pending_user_payment_view?.visibility = View.VISIBLE

        val placeholder = getString(R.string.async_steps_1_no_notification)
        val stepOneText = String.format(placeholder, paymentMethodLabel)

        step_one_desc.text = stepOneText

        pending_user_payment_view?.in_progress_animation?.updateBitmap("image_0", paymentMethodIcon)
        pending_user_payment_view?.in_progress_animation?.updateBitmap("image_1", applicationIcon)

    }

     fun showError(message: Int?) {
        LocalPaymentView.ViewState.ERROR
        error_message.text = getString(R.string.ok)
        message?.let { errorMessage = it }
        error_message.text = getString(message ?: errorMessage)
        pending_user_payment_view.visibility = View.GONE
        complete_payment_view.visibility = View.GONE
        pending_user_payment_view.in_progress_animation.cancelAnimation()
        complete_payment_view.lottie_transaction_success.cancelAnimation()
        progress_bar.visibility = View.GONE
        error_view.visibility = View.VISIBLE
    }

     fun dismissError() {
       LocalPaymentView.ViewState.NONE
        error_view.visibility = View.GONE

    }

     fun close() {
        LocalPaymentView.ViewState.NONE
        progress_bar.visibility = View.GONE
        error_view.visibility = View.GONE
        pending_user_payment_view.in_progress_animation.cancelAnimation()
        complete_payment_view.lottie_transaction_success.cancelAnimation()
        pending_user_payment_view.visibility = View.GONE
        complete_payment_view.visibility = View.GONE

    }

     fun getAnimationDuration() = complete_payment_view.lottie_transaction_success.duration

     fun popView(bundle: Bundle, paymentId: String) {
        bundle.putString(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY, paymentId)

    }

    enum class ViewState {
        NONE, COMPLETED, PENDING_USER_PAYMENT, ERROR, LOADING
    }

}
