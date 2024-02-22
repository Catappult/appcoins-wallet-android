package com.asfoundation.wallet.onboarding_new_payment.wallet_one

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingWalletOneLayoutBinding
import com.asfoundation.wallet.billing.wallet_one.WalletOneReturnSchemas
import com.asfoundation.wallet.onboarding_new_payment.getPurchaseBonusMessage
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingWalletOneFragment : BasePageViewFragment() {

  private val viewModel: OnboardingWalletOneViewModel by viewModels()
  private var binding: OnboardingWalletOneLayoutBinding? = null
  private val views get() = binding!!
  lateinit var args: OnboardingWalletOneFragmentArgs

  private lateinit var compositeDisposable: CompositeDisposable

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var navigator: OnboardingWalletOneNavigator

  private lateinit var resultAuthLauncher: ActivityResultLauncher<Intent>

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    binding = OnboardingWalletOneLayoutBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
    registerWebViewResult()
    return views.root
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    lockRotation()
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    args = OnboardingWalletOneFragmentArgs.fromBundle(requireArguments())
    setListeners()
    setObserver()
    startPayment()
  }

  private fun setObserver() {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state) {
        OnboardingWalletOneViewModel.State.Start -> {
        }

        is OnboardingWalletOneViewModel.State.Error -> {
          showError(getString(state.stringRes))
        }

        is OnboardingWalletOneViewModel.State.SuccessPurchase -> {
          handleSuccess()
        }

        is OnboardingWalletOneViewModel.State.WebAuthentication -> {
          startWebViewAuthorization(state.htmlData)
        }

        OnboardingWalletOneViewModel.State.WalletOneBack -> {
          findNavController().popBackStack(
            R.id.onboarding_payment_methods_fragment,
            inclusive = false
          )
        }

        is OnboardingWalletOneViewModel.State.BackToGame -> {
          navigator.navigateBackToGame(state.domain)
        }

        OnboardingWalletOneViewModel.State.ExploreWallet -> {
          navigator.navigateToHome()
        }
      }
    }
  }

  private fun startPayment() {
    viewModel.startPayment(
      amount = BigDecimal(args.amount),
      currency = args.currency,
      transactionBuilder = args.transactionBuilder,
      origin = args.transactionBuilder.origin
    )
  }

  private fun setListeners() {
    views.onboardingSuccessWalletOneButtons.backToGameButton.setOnClickListener {
      viewModel.handleBackToGameClick()
    }
    views.onboardingSuccessWalletOneButtons.exploreWalletButton.setOnClickListener {
      viewModel.handleExploreWalletClick()
    }
    views.errorTryAgainWalletOne.setOnClickListener {
      findNavController().popBackStack(R.id.onboarding_payment_methods_fragment, inclusive = false)
    }
    views.errorView.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport()
    }
    views.errorView.layoutSupportLogo.setOnClickListener {
      viewModel.showSupport()
    }
  }

  private fun startWebViewAuthorization(htmlData: String) {
    val intent = WebViewActivity.newIntentFromData(requireActivity(), htmlData)
    resultAuthLauncher.launch(intent)
  }

  private fun registerWebViewResult() {
    resultAuthLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when {
          result.data?.dataString?.contains(WalletOneReturnSchemas.SUCCESS.schema) == true -> {
            Log.d(this.tag, "startWebViewAuthorization SUCCESS: ${result.data ?: ""}")
            viewModel.waitForSuccess(
              viewModel.uid,
              args.transactionBuilder,
              false
            )
          }

          result.data?.dataString?.contains(WalletOneReturnSchemas.ERROR.schema) == true -> {
            Log.d(this.tag, "startWebViewAuthorization ERROR: ${result.data ?: ""}")
            viewModel._state
              .postValue(
                OnboardingWalletOneViewModel.State.Error(
                  R.string.purchase_error_one_wallet_generic
                )
              )
          }

          result.resultCode == Activity.RESULT_CANCELED -> {
            Log.d(this.tag, "startWebViewAuthorization CANCELED: ${result.data ?: ""}")
            findNavController().popBackStack(
              R.id.onboarding_payment_methods_fragment, inclusive = false
            )
          }

        }
      }
  }

  fun showError(message: String) {
    views.loadingAuthorizationAnimation.visibility = View.GONE
    views.noNetwork.root.visibility = View.GONE
    views.errorView.errorMessage.text = message
    views.errorView.root.visibility = View.VISIBLE
    views.errorTryAgainWalletOne.visibility = View.VISIBLE
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
    views.onboardingSuccessWalletOneButtons.root.visibility = View.VISIBLE
  }

  fun lockRotation() {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

}
