package com.asfoundation.wallet.onboarding_new_payment.google_pay

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingGooglePayLayoutBinding
import com.asfoundation.wallet.onboarding_new_payment.getPurchaseBonusMessage
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingGooglePayFragment : BasePageViewFragment() {

  private val viewModel: OnboardingGooglePayViewModel by viewModels()
  private var binding: OnboardingGooglePayLayoutBinding? = null
  private val views get() = binding!!
  lateinit var args: OnboardingGooglePayFragmentArgs

  private lateinit var compositeDisposable: CompositeDisposable

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: OnboardingGooglePayNavigator

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    binding = OnboardingGooglePayLayoutBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
    return views.root
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    lockRotation()
  }

  override fun onResume() {
    super.onResume()
    // checks success/error/cancel
    viewModel.processGooglePayResult(transactionBuilder = args.transactionBuilder)
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    args = OnboardingGooglePayFragmentArgs.fromBundle(requireArguments())
    setListeners()
    setObserver()
    startPayment()
  }

  private fun setObserver() {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state) {
        OnboardingGooglePayViewModel.State.Start -> {
        }

        is OnboardingGooglePayViewModel.State.Error -> {
          showError(getString(state.stringRes))
        }

        is OnboardingGooglePayViewModel.State.SuccessPurchase -> {
          handleSuccess()
        }

        is OnboardingGooglePayViewModel.State.WebAuthentication -> {
          viewModel.openUrlCustomTab(requireContext(), state.url)
        }

        OnboardingGooglePayViewModel.State.GooglePayBack -> {
          findNavController().popBackStack(
            R.id.onboarding_payment_methods_fragment,
            inclusive = false
          )
        }

        is OnboardingGooglePayViewModel.State.BackToGame -> {
          navigator.navigateBackToGame(state.domain)
        }

        OnboardingGooglePayViewModel.State.ExploreWallet -> {
          navigator.navigateToHome()
        }
      }
    }
  }

  private fun startPayment() {
    if (args.transactionBuilder.type == "INAPP") {
      args.transactionBuilder.referrerUrl = null
    }
    viewModel.startPayment(
      amount = BigDecimal(args.amount),
      currency = args.currency,
      transactionBuilder = args.transactionBuilder,
      origin = args.transactionBuilder.origin
    )
  }

  private fun setListeners() {
    views.onboardingSuccessGooglePayButtons.backToGameButton.setOnClickListener {
      viewModel.handleBackToGameClick()
    }
    views.onboardingSuccessGooglePayButtons.exploreWalletButton.setOnClickListener {
      viewModel.handleExploreWalletClick()
    }
    views.errorTryAgainGooglePay.setOnClickListener {
      findNavController().popBackStack(R.id.onboarding_payment_methods_fragment, inclusive = false)
    }
    views.errorView.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport()
    }
    views.errorView.layoutSupportLogo.setOnClickListener {
      viewModel.showSupport()
    }
  }

  fun showError(message: String) {
    views.loadingAuthorizationAnimation.visibility = View.GONE
    views.noNetwork.root.visibility = View.GONE
    views.errorView.errorMessage.text = message
    views.errorView.root.visibility = View.VISIBLE
    views.errorTryAgainGooglePay.visibility = View.VISIBLE
  }

  private fun handleSuccess() {
    views.fragmentFirstIabTransactionCompleted.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    val bonus = args.forecastBonus.getPurchaseBonusMessage(formatter)
    if (!bonus.isNullOrEmpty()) {
      views.fragmentFirstIabTransactionCompleted.transactionSuccessBonusText.text =
        getString(R.string.purchase_success_bonus_received_title, bonus)
    } else {
      views.fragmentFirstIabTransactionCompleted.bonusSuccessLayout.visibility = View.GONE
    }
    views.loadingAuthorizationAnimation.visibility = View.GONE
    views.errorView.root.visibility = View.GONE
    views.completePaymentView.visibility = View.VISIBLE
    views.fragmentFirstIabTransactionCompleted.iabFirstActivityTransactionCompleted.visibility =
      View.VISIBLE
    views.fragmentFirstIabTransactionCompleted.lottieTransactionSuccess.playAnimation()
    views.onboardingSuccessGooglePayButtons.root.visibility = View.VISIBLE
  }

  fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

}
