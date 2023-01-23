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
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingAdyenPaymentFragmentBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.util.AdyenCardView
import com.asfoundation.wallet.util.KeyboardUtils
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
        when (result.resultCode) {
          WEB_VIEW_REQUEST_CODE -> viewModel.handleWebViewResult(result)
        }
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

  private fun setupUi() {
    adyenCardView = AdyenCardView(views.onboardingAdyenPaymentCardView)
    setupConfiguration()
    handleBuyButtonText()
  }

  override fun onStateChanged(state: OnboardingAdyenPaymentState) {
    when (state.paymentInfoModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        views.loadingAnimation.playAnimation()
      }
      is Async.Success -> {
        state.paymentInfoModel()?.let {
          when {
            args.paymentType == PaymentType.CARD -> {
              prepareCardComponent(it)
            }
            args.paymentType == PaymentType.PAYPAL -> {
              viewModel.handlePaypal(it, RedirectComponent.getReturnUrl(requireContext()))
            }
            else -> Unit
          }
        }
      }
      is Async.Fail -> {

      }
    }
  }

  private fun prepareCardComponent(paymentInfoModel: PaymentInfoModel) {
    views.onboardingAdyenPaymentTitle.visibility = View.VISIBLE
    views.onboardingAdyenPaymentCardView.visibility = View.VISIBLE
    views.onboardingAdyenPaymentButtons.root.visibility = View.VISIBLE
    views.loadingAnimation.visibility = View.GONE

    adyenCardComponent = paymentInfoModel.cardComponent!!(this, cardConfiguration)
    views.onboardingAdyenPaymentCardView.attach(adyenCardComponent, this)
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
      is OnboardingAdyenPaymentSideEffect.NavigateToPaypal -> navigator.navigateToWebView(sideEffect.redirectUrl)
      is OnboardingAdyenPaymentSideEffect.HandleWebViewResult -> redirectComponent.handleIntent(
        Intent("", sideEffect.uri)
      )
      OnboardingAdyenPaymentSideEffect.NavigateBackToPaymentMethods -> navigator.navigateBack()
      OnboardingAdyenPaymentSideEffect.ShowCvvError -> TODO()
      OnboardingAdyenPaymentSideEffect.ShowLoading -> showLoading()
    }
  }

  private fun showLoading() {
    views.onboardingAdyenPaymentTitle.visibility = View.GONE
    views.onboardingAdyenPaymentCardView.visibility = View.GONE
    views.onboardingAdyenPaymentButtons.root.visibility = View.GONE
    views.loadingAnimation.visibility = View.VISIBLE
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

  fun setup3DSComponent() {
    activity?.application?.let { application ->
      adyen3DS2Component =
        Adyen3DS2Component.PROVIDER.get(this, application, adyen3DS2Configuration)
      adyen3DS2Component.observe(this) { actionComponentData ->
        viewModel.handleRedirectComponentResponse(actionComponentData)
      }
      adyen3DS2Component.observeErrors(this) { componentError ->
        viewModel.handle3DSErrors(componentError)
      }
    }
  }

  fun setupRedirectComponent() {
    activity?.application?.let { application ->
      redirectComponent = RedirectComponent.PROVIDER.get(this, application, redirectConfiguration)
      redirectComponent.observe(this) { actionComponentData ->
        viewModel.handleRedirectComponentResponse(actionComponentData)
      }
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

  companion object {
    const val WEB_VIEW_REQUEST_CODE = 1234
  }
}