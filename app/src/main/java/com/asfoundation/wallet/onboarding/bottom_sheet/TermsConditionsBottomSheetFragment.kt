package com.asfoundation.wallet.onboarding.bottom_sheet

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
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.TermsConditionsBottomSheetBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TermsConditionsBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<TermsConditionsBottomSheetState, TermsConditionsBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: TermsConditionsBottomSheetNavigator

  private val viewModel: TermsConditionsBottomSheetViewModel by viewModels()
  private val views by viewBinding(TermsConditionsBottomSheetBinding::bind)

  companion object {
    const val TERMS_CONDITIONS_COMPLETE = "terms_conditions_complete"

    @JvmStatic
    fun newInstance(): TermsConditionsBottomSheetFragment {
      return TermsConditionsBottomSheetFragment()
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.terms_conditions_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.termsConditionsConfirmButton.setOnClickListener { viewModel.handleCreateWallet() }
    views.termsConditionsDeclineButton.setOnClickListener { viewModel.handleDeclineClick() }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogTheme
  }

  override fun onStateChanged(state: TermsConditionsBottomSheetState) {
    setStringWithLinks()

  }

  override fun onSideEffect(sideEffect: TermsConditionsBottomSheetSideEffect) {
    when (sideEffect) {
      TermsConditionsBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
      TermsConditionsBottomSheetSideEffect.NavigateToFinish -> {
        navigator.navigateBack()
        setFragmentResult(
          TERMS_CONDITIONS_COMPLETE,
          bundleOf("fragmentEnded" to "result")
        )
      }
      TermsConditionsBottomSheetSideEffect.NavigateToWalletCreationAnimation ->{
        navigator.navigateToCreateWalletDialog()
        setFragmentResult(
          TERMS_CONDITIONS_COMPLETE,
          bundleOf("fragmentEnded" to "result")
        )
      }
      is TermsConditionsBottomSheetSideEffect.NavigateToLink -> navigator.navigateToBrowser(
        sideEffect.uri
      )
    }
  }

  private fun setStringWithLinks() {
    val termsConditions = resources.getString(R.string.terms_and_conditions)
    val privacyPolicy = resources.getString(R.string.privacy_policy)
    val termsPolicyTickBox =
      resources.getString(
        R.string.terms_and_conditions_tickbox, termsConditions,
        privacyPolicy
      )

    val spannableString = SpannableString(termsPolicyTickBox)
    setLinkToString(spannableString, termsConditions, Uri.parse(BuildConfig.TERMS_CONDITIONS_URL))
    setLinkToString(spannableString, privacyPolicy, Uri.parse(BuildConfig.PRIVACY_POLICY_URL))

    views.termsConditionsBody.text = spannableString
    views.termsConditionsBody.isClickable = true
    views.termsConditionsBody.movementMethod = LinkMovementMethod.getInstance()
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
        ds.color = ResourcesCompat.getColor(resources, R.color.appc_pink, null)
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
