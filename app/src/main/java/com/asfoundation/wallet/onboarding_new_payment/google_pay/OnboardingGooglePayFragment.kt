package com.asfoundation.wallet.onboarding_new_payment.google_pay

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_FATAL_ERROR
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_SUCCESS
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_USER_CANCEL
import com.asfoundation.wallet.onboarding_new_payment.utils.OnboardingUtils
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingGooglePayFragment : BasePageViewFragment() {

  private val viewModel: OnboardingGooglePayViewModel by viewModels()
  private var binding: OnboardingGooglePayLayoutBinding? = null
  private val views get() = binding!!
  lateinit var args: OnboardingGooglePayFragmentArgs
  private val clientWebSocket = OkHttpClient()

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
          viewModel.setResponseCodeWebSocket(SDK_STATUS_FATAL_ERROR)
          createWebSocketSdk()
          showError(getString(state.stringRes))
        }

        is OnboardingGooglePayViewModel.State.SuccessPurchase -> {
          viewModel.setResponseCodeWebSocket(SDK_STATUS_SUCCESS)
          createWebSocketSdk()
          handleSuccess()
        }

        is OnboardingGooglePayViewModel.State.WebAuthentication -> {
          viewModel.openUrlCustomTab(requireContext(), state.url)
        }

        OnboardingGooglePayViewModel.State.GooglePayBack -> {
          viewModel.setResponseCodeWebSocket(SDK_STATUS_USER_CANCEL)
          createWebSocketSdk()
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

  private fun createWebSocketSdk() {
    if (args.transactionBuilder.type == "INAPP") {
      if (args.transactionBuilder.wspPort == null &&
        OnboardingUtils.isSdkVersionAtLeast2(args.transactionBuilder.sdkVersion)
      ) {
        val responseCode = viewModel.getResponseCodeWebSocket()
        val productToken = viewModel.purchaseUid
        val purchaseResultJson = JSONObject().apply {
          put("responseCode", responseCode)
          put("purchaseToken", productToken)
        }.toString()

        val encodedPurchaseResult = Uri.encode(purchaseResultJson)

        val deepLinkUri = Uri.Builder()
          .scheme("web-iap-result")
          .authority(args.transactionBuilder.domain)
          .appendQueryParameter("purchaseResult", encodedPurchaseResult)
          .build()

        val deepLinkIntent = Intent(Intent.ACTION_VIEW, deepLinkUri)

        deepLinkIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        Handler(Looper.getMainLooper()).postDelayed({
          startActivity(deepLinkIntent)
        }, 2000)

      } else {
        val request = try {
          Request.Builder().url("ws://localhost:".plus(args.transactionBuilder.wspPort)).build()
        } catch (e: IllegalArgumentException) {
          null
        }
        val listener = SdkPaymentWebSocketListener(
          viewModel.purchaseUid,
          viewModel.uid,
          viewModel.getResponseCodeWebSocket()
        )
        request?.let {
          clientWebSocket.newWebSocket(request, listener)
        }
      }
    }
  }

  fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

}
