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
import android.view.animation.AnimationUtils
import androidx.annotation.Nullable
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.properties.PRIVACY_POLICY_URL
import com.appcoins.wallet.core.utils.properties.TERMS_CONDITIONS_URL
import com.asf.wallet.R
import com.asf.wallet.databinding.OnboardingPaymentMethodsFragmentBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.PaymentMethodClick
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.PaymentMethodsController
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodsMapper
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingPaymentMethodsFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null),
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
        PaymentMethodClick.OtherPaymentMethods -> viewModel.handleBackToGameClick()
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
        if (state.paymentMethodsAsync()?.isEmpty() == true) {
          handleNoPaymentMethodsError()
        } else {
          showPaymentMethodsList(state.paymentMethodsAsync(), state.otherPaymentMethods)
        }
      }
      is Async.Fail -> {
        handleNoPaymentMethodsError()
      }
    }
  }

  override fun onSideEffect(sideEffect: OnboardingPaymentMethodsSideEffect) {
    when (sideEffect) {
      is OnboardingPaymentMethodsSideEffect.NavigateToLink -> navigator.navigateToBrowser(sideEffect.uri)
      is OnboardingPaymentMethodsSideEffect.NavigateBackToGame -> navigator.navigateBackToGame(sideEffect.appPackageName)
    }
  }

  private fun showPaymentMethodsList(
    paymentMethodsList: List<PaymentMethod>?,
    otherPaymentMethodsList: List<PaymentMethod>
  ) {
    views.onboardingPaymentMethodsTitle.visibility = View.VISIBLE
    views.onboardingPaymentMethodsRv.visibility = View.VISIBLE
    views.onboardingPaymentTermsConditions.root.visibility = View.VISIBLE
    controller.setData(paymentMethodsList, otherPaymentMethodsList, paymentMethodsMapper)
  }

  private fun handleNoPaymentMethodsError() {
    views.onboardingPaymentMethodsTitle.visibility = View.GONE
    views.onboardingPaymentMethodsRv.visibility = View.GONE
    views.onboardingPaymentTermsConditions.root.visibility = View.GONE
    views.noPaymentMethodsError.root.startAnimation(
      AnimationUtils.loadAnimation(
        context,
        R.anim.pop_in_animation
      )
    )
    views.noPaymentMethodsError.root.visibility = View.VISIBLE
    views.onboardingIncompletePaymentMethods.root.visibility = View.VISIBLE
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
        R.string.agree_by_choosing_a_payment_method_body, termsConditions,
        privacyPolicy
      )

    val spannableString = SpannableString(termsPolicyTickBox)
    setLinkToString(spannableString, termsConditions, Uri.parse(TERMS_CONDITIONS_URL))
    setLinkToString(spannableString, privacyPolicy, Uri.parse(PRIVACY_POLICY_URL))

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