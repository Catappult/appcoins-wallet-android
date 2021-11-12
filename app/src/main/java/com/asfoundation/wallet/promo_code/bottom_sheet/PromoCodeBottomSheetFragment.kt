package com.asfoundation.wallet.promo_code.bottom_sheet


import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.SettingsPromoCodeBottomSheetLayoutBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeEntity
import com.asfoundation.wallet.util.KeyboardUtils
import com.asfoundation.wallet.util.setReadOnly
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class PromoCodeBottomSheetFragment : DaggerBottomSheetDialogFragment(),
    SingleStateFragment<PromoCodeBottomSheetState, PromoCodeBottomSheetSideEffect> {


  @Inject
  lateinit var promoCodeBottomSheetViewModelFactory: PromoCodeBottomSheetViewModelFactory

  @Inject
  lateinit var navigator: PromoCodeBottomSheetNavigator

  private val viewModel: PromoCodeBottomSheetViewModel by viewModels { promoCodeBottomSheetViewModelFactory }
  private val views by viewBinding(SettingsPromoCodeBottomSheetLayoutBinding::bind)

  companion object {
    @JvmStatic
    fun newInstance(): PromoCodeBottomSheetFragment {
      return PromoCodeBottomSheetFragment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.settings_promo_code_bottom_sheet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.promoCodeBottomSheetSubmitButton.setOnClickListener {
      viewModel.submitClick(views.promoCodeBottomSheetString.text.toString())
    }
    views.promoCodeBottomSheetReplaceButton.setOnClickListener {
      viewModel.replaceClick()
    }
    views.promoCodeBottomSheetDeleteButton.setOnClickListener { viewModel.deleteClick() }
    views.promoCodeBottomSheetSuccessGotItButton.setOnClickListener { viewModel.successGotItClick() }

    views.promoCodeBottomSheetString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.promoCodeBottomSheetSubmitButton.isEnabled = s.isNotEmpty()
      }

      override fun afterTextChanged(s: Editable) = Unit
    })

    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeNoFloating
  }

  override fun onStateChanged(state: PromoCodeBottomSheetState) {
    setPromoCode(state.promoCodeAsync, state.shouldShowDefault)
    setSubmitClick(state.submitClickAsync, state.promoCodeAsync)
  }

  override fun onSideEffect(sideEffect: PromoCodeBottomSheetSideEffect) {
    when (sideEffect) {
      is PromoCodeBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  fun setPromoCode(promoCodeAsync: Async<PromoCode>,
                   shouldShowDefault: Boolean) {
    when (promoCodeAsync) {
      is Async.Uninitialized,
      is Async.Loading -> {
        showDefaultScreen()
      }
      is Async.Fail -> {
        showErrorMessage()
      }
      is Async.Success -> {
        if (shouldShowDefault) {
          showDefaultScreen()
        } else {
          if (promoCodeAsync.value?.code != null) {
            showCurrentCodeScreen(promoCodeAsync.value.code)
          }
        }
      }
    }
  }

  fun setSubmitClick(clickAsync: Async<Unit>, promoCodeAsync: Async<PromoCode>) {
    when (clickAsync) {
      is Async.Uninitialized -> {
      }
      is Async.Loading -> {
        if (clickAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
        showErrorMessage()
      }
      is Async.Success -> {
        promoCodeAsync.value?.bonus?.let { showSuccess(it) }
      }
    }
  }

  private fun showErrorMessage() {
    hideAll()
    views.promoCodeBottomSheetString.setReadOnly(false, InputType.TYPE_CLASS_TEXT)
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetTextRectangle.visibility = View.VISIBLE
    views.promoCodeBottomSheetTextRectangle.setBackgroundResource(R.drawable.rectangle_outline_red)
    views.promoCodeBottomSheetErrorMessage.visibility = View.VISIBLE
    views.promoCodeBottomSheetSubmitButton.visibility = View.VISIBLE
    views.promoCodeBottomSheetSubmitButton.isEnabled = false
  }

  private fun showLoading() {
    hideAll()
    views.promoCodeBottomSheetSystemView.visibility = View.VISIBLE
    views.promoCodeBottomSheetSystemView.showProgress(true)
  }

  private fun showDefaultScreen() {
    hideAll()
    views.promoCodeBottomSheetString.setReadOnly(false, InputType.TYPE_CLASS_TEXT)
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetTextRectangle.visibility = View.VISIBLE
    views.promoCodeBottomSheetTextRectangle.setBackgroundResource(
        R.drawable.rectangle_text_default_promo_code)
    views.promoCodeBottomSheetSubmitButton.visibility = View.VISIBLE
    views.promoCodeBottomSheetSubmitButton.isEnabled = false
  }

  private fun showCurrentCodeScreen(promoCodeString: String) {
    hideAll()
    views.promoCodeBottomSheetString.text = Editable.Factory.getInstance()
        .newEditable(promoCodeString)
    views.promoCodeBottomSheetString.setReadOnly(true)
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetTextRectangle.visibility = View.VISIBLE
    views.promoCodeBottomSheetTextRectangle.setBackgroundResource(
        R.drawable.rectangle_text_active_promo_code)
    views.promoCodeBottomSheetActiveCheckmark.visibility = View.VISIBLE
    views.promoCodeBottomSheetDeleteButton.visibility = View.VISIBLE
    views.promoCodeBottomSheetReplaceButton.visibility = View.VISIBLE
  }

  @SuppressLint("StringFormatMatches")
  private fun showSuccess(bonus: Double) {
    hideAll()
    KeyboardUtils.hideKeyboard(view)
    views.promoCodeBottomSheetSuccessAnimation.visibility = View.VISIBLE
    views.promoCodeBottomSheetSuccessAnimation.setAnimation(R.raw.success_animation)
    views.promoCodeBottomSheetSuccessAnimation.setAnimation(R.raw.success_animation)
    views.promoCodeBottomSheetSuccessAnimation.repeatCount = 0
    views.promoCodeBottomSheetSuccessAnimation.playAnimation()
    views.promoCodeBottomSheetSuccessTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetSuccessSubtitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetSuccessSubtitle.text =
        this.getString(R.string.promo_code_success_body, bonus.toString())
    views.promoCodeBottomSheetSuccessGotItButton.visibility = View.VISIBLE

  }

  private fun hideAll() {
    hideDefaultScreen()
    hideButtons()
    hideLoading()
    hideErrorMessage()
    hideSuccessScreen()
  }

  private fun hideDefaultScreen() {
    views.promoCodeBottomSheetTitle.visibility = View.GONE
    views.promoCodeBottomSheetTextRectangle.visibility = View.GONE
    views.promoCodeBottomSheetActiveCheckmark.visibility = View.GONE
  }

  private fun hideButtons() {
    views.promoCodeBottomSheetSubmitButton.visibility = View.GONE
    views.promoCodeBottomSheetReplaceButton.visibility = View.GONE
    views.promoCodeBottomSheetDeleteButton.visibility = View.GONE
  }

  private fun hideLoading() {
    views.promoCodeBottomSheetSystemView.visibility = View.GONE
  }

  private fun hideErrorMessage() {
    views.promoCodeBottomSheetErrorMessage.visibility = View.GONE
  }

  private fun hideSuccessScreen() {
    views.promoCodeBottomSheetSuccessAnimation.visibility = View.GONE
    views.promoCodeBottomSheetSuccessTitle.visibility = View.GONE
    views.promoCodeBottomSheetSuccessSubtitle.visibility = View.GONE
    views.promoCodeBottomSheetSuccessGotItButton.visibility = View.GONE
  }
}