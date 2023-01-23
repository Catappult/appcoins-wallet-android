package com.asfoundation.wallet.onboarding_new_payment.payment_methods

import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingPaymentMethodsFragmentBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.PaymentMethodClick
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.PaymentMethodsController
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodsMapper
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingPaymentMethodsFragment : BasePageViewFragment(),
  SingleStateFragment<OnboardingPaymentMethodsState, OnboardingPaymentMethodsSideEffect> {

  private val viewModel: OnboardingPaymentMethodsViewModel by viewModels()
  private val views by viewBinding(OnboardingPaymentMethodsFragmentBinding::bind)
  lateinit var args: OnboardingPaymentMethodsFragmentArgs

  @Inject
  lateinit var navigator: OnboardingPaymentMethodsNavigator

  @Inject
  lateinit var paymentMethodsMapper: PaymentMethodsMapper

  private lateinit var controller: PaymentMethodsController

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    return OnboardingPaymentMethodsFragmentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    args = OnboardingPaymentMethodsFragmentArgs.fromBundle(requireArguments())
    handlePaymentMethodList()
    setStringWithLinks()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun handlePaymentMethodList() {
    controller = PaymentMethodsController()
    controller.clickListener = { paymentMethodClick ->
      when (paymentMethodClick) {
        is PaymentMethodClick.CreditCardClick -> navigator.navigateToAdyen(
          args.transactionBuilder,
          args.amount,
          args.currency,
          args.forecastBonus
        )
        is PaymentMethodClick.PaypalAdyenClick -> navigator.navigateToPaypalAdyen(
          args.transactionBuilder,
          args.amount,
          args.currency,
          args.forecastBonus
        )
        is PaymentMethodClick.PaypalDirectClick -> navigator.navigateToPaypalDirect()
        is PaymentMethodClick.LocalPaymentClick -> navigator.navigateToLocalPayment()
        is PaymentMethodClick.CarrierBillingClick -> navigator.navigateToCarrierBilling()
        is PaymentMethodClick.ShareLinkPaymentClick -> navigator.navigateToShareLinkPayment()
      }
    }
    views.onboardingPaymentMethodsRv.setController(controller)
  }

  override fun onStateChanged(state: OnboardingPaymentMethodsState) {
    when (state.paymentMethodsAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        //TODO add a skeleton while the list loads
      }
      is Async.Success -> {
        showPaymentMethodsList(state.paymentMethodsAsync())
      }
      is Async.Fail -> Unit
    }
  }

  private fun showPaymentMethodsList(paymentMethodsAsync: List<PaymentMethod>?) {
    views.onboardingPaymentMethodsTitle.visibility = View.VISIBLE
    views.onboardingPaymentMethodsRv.visibility = View.VISIBLE
    views.onboardingPaymentTermsConditions.root.visibility = View.VISIBLE
    controller.setData(paymentMethodsAsync, paymentMethodsMapper)
  }

  override fun onSideEffect(sideEffect: OnboardingPaymentMethodsSideEffect) {
    when (sideEffect) {
      is OnboardingPaymentMethodsSideEffect.NavigateToLink -> navigator.navigateToBrowser(sideEffect.uri)
    }
  }

  /**
   * setStringWithLinks() and setLinkToString() is duplicated from the OnboardingFragment and
   * should be extracted to a extension function to avoid this.
   * The action calls the viewModel and this should be set as an argument (the action)
   * to be able to extract this logic for usage in both places
   */
  private fun setStringWithLinks() {
    val termsConditions = resources.getString(R.string.terms_and_conditions)
    val privacyPolicy = resources.getString(R.string.privacy_policy)
    val termsPolicyTickBox =
      resources.getString(
        R.string.intro_agree_terms_and_conditions_body, termsConditions,
        privacyPolicy
      )

    val spannableString = SpannableString(termsPolicyTickBox)
    setLinkToString(spannableString, termsConditions, Uri.parse(BuildConfig.TERMS_CONDITIONS_URL))
    setLinkToString(spannableString, privacyPolicy, Uri.parse(BuildConfig.PRIVACY_POLICY_URL))

    views.onboardingPaymentTermsConditions.termsConditionsBody.text = spannableString
    views.onboardingPaymentTermsConditions.termsConditionsBody.isClickable = true
    views.onboardingPaymentTermsConditions.termsConditionsBody.movementMethod =
      LinkMovementMethod.getInstance()
  }

  private fun setLinkToString(
    spannableString: SpannableString, highlightString: String,
    uri: Uri
  ) {
    val clickableSpan = object : ClickableSpan() {
      override fun onClick(widget: View) {
        viewModel.handleLinkClick(uri = uri)
      }

      override fun updateDrawState(ds: TextPaint) {
        ds.color = ResourcesCompat.getColor(resources, R.color.styleguide_pink, null)
        ds.isUnderlineText = true
      }
    }
    val indexHighlightString = spannableString.toString()
      .indexOf(highlightString)
    val highlightStringLength = highlightString.length
    spannableString.setSpan(
      clickableSpan, indexHighlightString,
      indexHighlightString + highlightStringLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
      StyleSpan(Typeface.BOLD), indexHighlightString,
      indexHighlightString + highlightStringLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
  }
}