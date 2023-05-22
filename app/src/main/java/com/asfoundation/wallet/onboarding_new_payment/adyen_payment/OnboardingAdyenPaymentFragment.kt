package com.asfoundation.wallet.onboarding_new_payment.adyen_payment

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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingAdyenPaymentFragmentBinding
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.util.AdyenCardView
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingAdyenPaymentFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingAdyenPaymentState, OnboardingAdyenPaymentSideEffect> {

  private val viewModel: OnboardingAdyenPaymentViewModel by viewModels()
  private val views by viewBinding(OnboardingAdyenPaymentFragmentBinding::bind)
  lateinit var args: OnboardingAdyenPaymentFragmentArgs

  //configurations
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var redirectConfiguration: RedirectConfiguration
  private lateinit var adyen3DS2Configuration: Adyen3DS2Configuration

  //components
  private lateinit var adyenCardComponent: CardComponent
  private lateinit var redirectComponent: RedirectComponent
  private lateinit var adyen3DS2Component: Adyen3DS2Component
  private lateinit var adyenCardView: AdyenCardView
  private lateinit var adyenCardWrapper: AdyenCardWrapper

  private lateinit var webViewLauncher: ActivityResultLauncher<Intent>
  private lateinit var outerNavController: NavController

  @Inject
  lateinit var navigator: OnboardingAdyenPaymentNavigator

  @Inject
  lateinit var adyenEnvironment: Environment

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    return OnboardingAdyenPaymentFragmentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    args = OnboardingAdyenPaymentFragmentArgs.fromBundle(requireArguments())
    initOuterNavController()
    setupUi()
    clickListeners()
    createResultLauncher()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun createResultLauncher() {
    webViewLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.handleWebViewResult(result)
      }
  }

  private fun clickListeners() {
    views.onboardingAdyenPaymentButtons.adyenPaymentBackButton.setOnClickListener {
      viewModel.handleBackButton()
    }
    views.onboardingAdyenPaymentButtons.adyenPaymentBuyButton.setOnClickListener {
      viewModel.handleBuyClick(
        adyenCardWrapper,
        RedirectComponent.getReturnUrl(requireContext())
      )
    }
  }

  override fun onStateChanged(state: OnboardingAdyenPaymentState) {
    when (state.paymentInfoModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        views.loadingAnimation.playAnimation()
      }
      is Async.Success -> {
        state.paymentInfoModel()?.let {
          when (args.paymentType) {
            PaymentType.CARD -> {
              prepareCardComponent(it)
            }
            PaymentType.PAYPAL -> {
              viewModel.handlePaypal(it, RedirectComponent.getReturnUrl(requireContext()))
            }
            else -> Unit
          }
        }
      }
      is Async.Fail -> Unit
    }
  }

  override fun onSideEffect(sideEffect: OnboardingAdyenPaymentSideEffect) {
    when (sideEffect) {
      is OnboardingAdyenPaymentSideEffect.NavigateToPaymentResult -> navigator.navigateToPaymentResult(
        outerNavController,
        sideEffect.paymentModel,
        args.transactionBuilder,
        args.paymentType,
        args.amount,
        args.currency,
        args.forecastBonus
      )
      is OnboardingAdyenPaymentSideEffect.NavigateToWebView -> {
        sideEffect.paymentModel.redirectUrl?.let {
          navigator.navigateToWebView(
            it,
            webViewLauncher
          )
        }
      }
      is OnboardingAdyenPaymentSideEffect.HandleWebViewResult -> redirectComponent.handleIntent(
        Intent("", sideEffect.uri)
      )
      is OnboardingAdyenPaymentSideEffect.Handle3DS -> handle3DSAction(sideEffect.action)
      OnboardingAdyenPaymentSideEffect.NavigateBackToPaymentMethods -> navigator.navigateBack()
      OnboardingAdyenPaymentSideEffect.ShowCvvError -> handleCVCError()
      OnboardingAdyenPaymentSideEffect.ShowLoading -> showLoading(shouldShow = true)
    }
  }

  private fun setupUi() {
    adyenCardView = AdyenCardView(views.adyenCardFormPreSelected)
    setupConfiguration()
    setup3DSComponent()
    setupRedirectComponent()
    handleBuyButtonText()
  }

  private fun prepareCardComponent(paymentInfoModel: PaymentInfoModel) {
    views.onboardingAdyenPaymentTitle.visibility = View.VISIBLE
    views.adyenCardFormPreSelected.visibility = View.VISIBLE
    views.onboardingAdyenPaymentButtons.root.visibility = View.VISIBLE
    views.loadingAnimation.visibility = View.GONE

    adyenCardComponent = paymentInfoModel.cardComponent!!(this, cardConfiguration)
    views.adyenCardFormPreSelected.attach(adyenCardComponent, this)
    adyenCardComponent.observe(this) {
      if (it != null && it.isValid) {
        views.onboardingAdyenPaymentButtons.adyenPaymentBuyButton.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          adyenCardWrapper = AdyenCardWrapper(
            paymentMethod,
            adyenCardView.cardSave,
            hasCvc,
            paymentInfoModel.supportedShopperInteractions
          )
        }
      } else {
        views.onboardingAdyenPaymentButtons.adyenPaymentBuyButton.isEnabled = false
      }
    }
  }

  private fun initOuterNavController() {
    outerNavController = Navigation.findNavController(requireActivity(), R.id.full_host_container)
  }

  private fun showLoading(shouldShow: Boolean) {
    views.onboardingAdyenPaymentTitle.visibility = if (shouldShow) View.GONE else View.VISIBLE
    views.adyenCardFormPreSelected.visibility = if (shouldShow) View.GONE else View.VISIBLE
    views.onboardingAdyenPaymentButtons.root.visibility =
      if (shouldShow) View.GONE else View.VISIBLE
    views.loadingAnimation.visibility = if (shouldShow) View.VISIBLE else View.GONE
  }

  private fun setupConfiguration() {
    if (args.paymentType == PaymentType.CARD) setupCardConfiguration()
    setupRedirectConfiguration()
    setupAdyen3DS2Configuration()
  }

  private fun setupCardConfiguration() {
    cardConfiguration = CardConfiguration.Builder(requireContext(), BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment).build()
  }

  private fun setupRedirectConfiguration() {
    redirectConfiguration =
      RedirectConfiguration.Builder(requireContext(), BuildConfig.ADYEN_PUBLIC_KEY)
        .setEnvironment(adyenEnvironment).build()
  }

  private fun setupAdyen3DS2Configuration() {
    adyen3DS2Configuration =
      Adyen3DS2Configuration.Builder(requireContext(), BuildConfig.ADYEN_PUBLIC_KEY)
        .setEnvironment(adyenEnvironment).build()
  }

  private fun setup3DSComponent() {
    adyen3DS2Component =
      Adyen3DS2Component.PROVIDER.get(this, requireActivity().application, adyen3DS2Configuration)
    adyen3DS2Component.observe(this) { actionComponentData ->
      viewModel.handleRedirectComponentResponse(actionComponentData)
    }
    adyen3DS2Component.observeErrors(this) { componentError ->
      viewModel.handle3DSErrors(componentError)
    }
  }

  private fun setupRedirectComponent() {
    redirectComponent =
      RedirectComponent.PROVIDER.get(this, requireActivity().application, redirectConfiguration)
    redirectComponent.observe(this) { actionComponentData ->
      viewModel.handleRedirectComponentResponse(actionComponentData)
    }
  }

  private fun handleBuyButtonText() {
    when {
      args.transactionBuilder.type.equals(
        TransactionData.TransactionType.DONATION.name,
        ignoreCase = true
      ) -> {
        views.onboardingAdyenPaymentButtons.adyenPaymentBuyButton.setText(getString(R.string.action_donate))
      }
      args.transactionBuilder.type.equals(
        TransactionData.TransactionType.INAPP_SUBSCRIPTION.name,
        ignoreCase = true
      ) -> views.onboardingAdyenPaymentButtons.adyenPaymentBuyButton.setText(getString(R.string.subscriptions_subscribe_button))
      else -> {
        views.onboardingAdyenPaymentButtons.adyenPaymentBuyButton.setText(getString(R.string.action_buy))
      }
    }
  }

  private fun handle3DSAction(action: Action?) {
    action?.let {
      adyen3DS2Component.handleAction(requireActivity(), it)
    }
  }

  private fun handleCVCError() {
    showLoading(shouldShow = false)
    views.onboardingAdyenPaymentButtons.adyenPaymentBuyButton.isEnabled = false
    adyenCardView.setError(getString(R.string.purchase_card_error_CVV))
  }
}