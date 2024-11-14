package com.asfoundation.wallet.manage_cards

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
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
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.ui.widgets.TopBar
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.ManageAdyenPaymentFragmentBinding
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.google.android.material.textfield.TextInputLayout
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ManageAdyenPaymentFragment : BasePageViewFragment(),
  SingleStateFragment<ManageAdyenPaymentState, ManageAdyenPaymentSideEffect> {

  private val viewModel: ManageAdyenPaymentViewModel by viewModels()
  private val views by viewBinding(ManageAdyenPaymentFragmentBinding::bind)

  //configurations
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var redirectConfiguration: RedirectConfiguration
  private lateinit var adyen3DS2Configuration: Adyen3DS2Configuration

  //components
  private lateinit var adyenCardComponent: CardComponent
  private lateinit var redirectComponent: RedirectComponent
  private lateinit var adyen3DS2Component: Adyen3DS2Component
  private lateinit var adyenCardWrapper: AdyenCardWrapper

  private lateinit var webViewLauncher: ActivityResultLauncher<Intent>
  private lateinit var outerNavController: NavController

  @Inject
  lateinit var navigator: ManageAdyenPaymentNavigator

  @Inject
  lateinit var adyenEnvironment: Environment

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private val manageCardSharedViewModel: ManageCardSharedViewModel by activityViewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return ManageAdyenPaymentFragmentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    clickListeners()
    createResultLauncher()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    view.findViewById<ComposeView>(R.id.app_bar).apply {
      setContent {
        TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() }, fragmentName = fragmentName, buttonsAnalytics = buttonsAnalytics)
      }
    }
  }

  private fun clickListeners() {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
      navigator.navigateBack()
    }
    views.manageWalletAddCardSubmitButton.setOnClickListener {
      viewModel.handleBuyClick(
        adyenCardWrapper,
        RedirectComponent.getReturnUrl(requireContext())
      )
    }
  }

  private fun setupRedirectConfiguration() {
    redirectConfiguration =
      RedirectConfiguration.Builder(requireContext(), BuildConfig.ADYEN_PUBLIC_KEY)
        .setEnvironment(adyenEnvironment).build()
  }

  private fun setupRedirectComponent() {
    redirectComponent =
      RedirectComponent.PROVIDER.get(this, requireActivity().application, redirectConfiguration)
    redirectComponent.observe(this) { actionComponentData ->
      viewModel.handleRedirectComponentResponse(actionComponentData)
    }
  }

  override fun onStateChanged(state: ManageAdyenPaymentState) {
    when (state.paymentInfoModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        views.loadingAnimation.playAnimation()
      }

      is Async.Success -> {
        state.paymentInfoModel()?.let {
          prepareCardComponent(it)
        }
      }

      is Async.Fail -> Unit
    }
  }

  override fun onSideEffect(sideEffect: ManageAdyenPaymentSideEffect) {
    when (sideEffect) {
      is ManageAdyenPaymentSideEffect.NavigateToPaymentResult -> {
        manageCardSharedViewModel.onCardSaved()
        navigator.navigateBack()
      }
      is ManageAdyenPaymentSideEffect.NavigateToPaymentError -> {
        manageCardSharedViewModel.onCardError()
        navigator.navigateBack()
      }
      ManageAdyenPaymentSideEffect.NavigateBackToPaymentMethods -> navigator.navigateBack()
      ManageAdyenPaymentSideEffect.ShowLoading -> showLoading(shouldShow = true)
      is ManageAdyenPaymentSideEffect.Handle3DS -> handle3DSAction(sideEffect.action)
      is ManageAdyenPaymentSideEffect.HandleRedirect -> handleRedirect(sideEffect.url)
      is ManageAdyenPaymentSideEffect.HandleWebViewResult -> handleWebViewResult(sideEffect.uri)
      ManageAdyenPaymentSideEffect.ShowCvvError -> handleCVCError()
    }
  }

  private fun setupUi() {
    setupConfiguration()
    setup3DSComponent()
    setupRedirectComponent()
    manageCardSharedViewModel.resetCardResult()
  }

  private fun prepareCardComponent(paymentInfoModel: PaymentInfoModel) {
    showLoading(false)
    adyenCardComponent = paymentInfoModel.cardComponent!!(requireActivity(), cardConfiguration)
    views.adyenCardForm.attach(adyenCardComponent, this)
    adyenCardComponent.observe(this) {
      if (it != null && it.isValid) {
        views.manageWalletAddCardSubmitButton.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          adyenCardWrapper = AdyenCardWrapper(
            paymentMethod,
            true,
            hasCvc,
            paymentInfoModel.supportedShopperInteractions
          )
        }
      } else {
        views.manageWalletAddCardSubmitButton.isEnabled = false
      }
      hideRememberCardSwitch()
      disableScrollBars()
    }
  }

  private fun showLoading(shouldShow: Boolean) {
    views.manageAdyenPaymentTitle.visibility = if (shouldShow) View.GONE else View.VISIBLE
    views.adyenCardForm.visibility = if (shouldShow) View.GONE else View.VISIBLE
    views.manageWalletAddCardSubmitButton.visibility =
      if (shouldShow) View.GONE else View.VISIBLE
    views.loadingAnimation.visibility = if (shouldShow) View.VISIBLE else View.GONE
  }

  private fun setupConfiguration() {
    setupCardConfiguration()
    setupRedirectConfiguration()
    setupAdyen3DS2Configuration()
  }

  private fun setupCardConfiguration() {
    cardConfiguration = CardConfiguration.Builder(requireContext(), BuildConfig.ADYEN_PUBLIC_KEY)
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

  private fun handle3DSAction(action: Action?) {
    action?.let {
      adyen3DS2Component.handleAction(requireActivity(), it)
    }
  }

  private fun handleRedirect(url: String) {
    webViewLauncher.launch(WebViewActivity.newIntent(requireActivity(), url))
  }

  private fun createResultLauncher() {
    webViewLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.handleWebViewResult(result)
      }
  }

  private fun handleWebViewResult(uri: Uri) {
    redirectComponent.handleIntent(Intent("", uri))
  }
  private fun handleCVCError() {
    showLoading(shouldShow = false)
    views.manageWalletAddCardSubmitButton.isEnabled = false
    setErrorCVC()
  }

  private fun hideRememberCardSwitch() {
    views.adyenCardForm.findViewById<SwitchCompat>(R.id.switch_storePaymentMethod).visibility =
      View.INVISIBLE
  }

  private fun setErrorCVC() {
    views.adyenCardForm.findViewById<TextInputLayout>(R.id.textInputLayout_securityCode).error =
      getString(R.string.purchase_card_error_CVV)
  }

  private fun disableScrollBars() {
    views.adyenCardForm.findViewById<EditText>(R.id.editText_cardNumber)
      .isVerticalScrollBarEnabled = false
    views.adyenCardForm.findViewById<EditText>(R.id.editText_expiryDate)
      .isVerticalScrollBarEnabled = false
    views.adyenCardForm.findViewById<EditText>(R.id.editText_securityCode)
      .isVerticalScrollBarEnabled = false
  }

}
