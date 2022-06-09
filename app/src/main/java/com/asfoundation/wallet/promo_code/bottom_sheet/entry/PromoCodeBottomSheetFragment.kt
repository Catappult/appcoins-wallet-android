package com.asfoundation.wallet.promo_code.bottom_sheet.entry


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.asfoundation.wallet.promo_code.FailedPromoCode
import com.asfoundation.wallet.promo_code.bottom_sheet.PromoCodeBottomSheetNavigator
import com.asfoundation.wallet.promo_code.PromoCodeResult
import com.asfoundation.wallet.promo_code.SuccessfulPromoCode
import com.asfoundation.wallet.ui.common.WalletTextFieldView
import com.asfoundation.wallet.util.KeyboardUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromoCodeBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<PromoCodeBottomSheetState, PromoCodeBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: PromoCodeBottomSheetNavigator

  private val viewModel: PromoCodeBottomSheetViewModel by viewModels()
  private val views by viewBinding(SettingsPromoCodeBottomSheetLayoutBinding::bind)

  companion object {
    @JvmStatic
    fun newInstance(): PromoCodeBottomSheetFragment {
      return PromoCodeBottomSheetFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.settings_promo_code_bottom_sheet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setListeners()
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

  private fun setListeners() {
    views.promoCodeBottomSheetSubmitButton.setOnClickListener {
      viewModel.submitClick(views.promoCodeBottomSheetString.getText().trim())
    }
    views.promoCodeBottomSheetReplaceButton.setOnClickListener {
      viewModel.replaceClick()
    }
    views.promoCodeBottomSheetDeleteButton.setOnClickListener { viewModel.deleteClick() }

    views.promoCodeBottomSheetString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.promoCodeBottomSheetSubmitButton.isEnabled = s.isNotEmpty()
      }

      override fun afterTextChanged(s: Editable) = Unit
    })
  }

  override fun onStateChanged(state: PromoCodeBottomSheetState) {
    when (val clickAsync = state.submitClickAsync) {
      is Async.Uninitialized -> setPromoCode(state.promoCodeAsync, state.shouldShowDefault)
      is Async.Loading -> {
        if (clickAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
        handleErrorState(FailedPromoCode.InvalidCode(clickAsync.error.throwable))
      }
      is Async.Success -> {
        state.promoCodeAsync.value?.let { handleClickSuccessState(it) }
      }
    }
  }

  override fun onSideEffect(sideEffect: PromoCodeBottomSheetSideEffect) {
    when (sideEffect) {
      is PromoCodeBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  fun setPromoCode(
    promoCodeAsync: Async<PromoCodeResult>,
    shouldShowDefault: Boolean
  ) {
    when (promoCodeAsync) {
      is Async.Uninitialized,
      is Async.Loading -> {
        showDefaultScreen()
      }
      is Async.Fail -> {
        if (promoCodeAsync.value != null) {
          handleErrorState(FailedPromoCode.GenericError(promoCodeAsync.error.throwable))
        }
      }
      is Async.Success -> {
        promoCodeAsync.value?.let { handlePromoCodeSuccessState(it, shouldShowDefault) }
      }
    }
  }

  private fun handleClickSuccessState(promoCodeResult: PromoCodeResult) {
    when (promoCodeResult) {
      is SuccessfulPromoCode -> {
        promoCodeResult.promoCode.code?.let {
          KeyboardUtils.hideKeyboard(view)
          navigator.navigateToSuccess(promoCodeResult.promoCode)
        }
      }
      else -> handleErrorState(promoCodeResult)
    }
  }

  private fun handlePromoCodeSuccessState(
    promoCodeResult: PromoCodeResult,
    shouldShowDefault: Boolean
  ) {
    when (promoCodeResult) {
      is SuccessfulPromoCode -> {
        if (shouldShowDefault) {
          showDefaultScreen()
        } else {
          promoCodeResult.promoCode.code?.let { showCurrentCodeScreen(it) }
        }
      }
      else -> handleErrorState(promoCodeResult)
    }
  }

  private fun handleErrorState(promoCodeResult: PromoCodeResult) {
    showDefaultScreen()
    views.promoCodeBottomSheetSubmitButton.isEnabled = false
    when (promoCodeResult) {
      is FailedPromoCode.InvalidCode -> {
        views.promoCodeBottomSheetString.setError(getString(R.string.promo_code_view_error))
      }
      is FailedPromoCode.ExpiredCode -> {
        views.promoCodeBottomSheetString.setError(getString(R.string.promo_code_error_not_available))
      }
      is FailedPromoCode.GenericError -> {
        views.promoCodeBottomSheetString.setError(getString(R.string.promo_code_error_invalid_user))
      }
      else -> return
    }
  }

  private fun showLoading() {
    hideAll()
    views.promoCodeBottomSheetSystemView.visibility = View.VISIBLE
    views.promoCodeBottomSheetSystemView.showProgress(true)
  }

  private fun showDefaultScreen() {
    hideAll()
    views.promoCodeBottomSheetString.setType(WalletTextFieldView.Type.OUTLINED)
    views.promoCodeBottomSheetString.visibility = View.VISIBLE
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetActiveCheckmark.visibility = View.GONE
    views.promoCodeBottomSheetSubmitButton.visibility = View.VISIBLE
    views.promoCodeBottomSheetDeleteButton.visibility = View.GONE
    views.promoCodeBottomSheetReplaceButton.visibility = View.GONE
    views.promoCodeBottomSheetSubmitButton.isEnabled = false
  }

  private fun showCurrentCodeScreen(promoCodeString: String) {
    hideAll()
    views.promoCodeBottomSheetString.setType(WalletTextFieldView.Type.READ_ONLY)
    views.promoCodeBottomSheetString.setText(
      Editable.Factory.getInstance().newEditable(promoCodeString)
    )
    views.promoCodeBottomSheetString.visibility = View.VISIBLE
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetActiveCheckmark.visibility = View.VISIBLE
    views.promoCodeBottomSheetSubmitButton.visibility = View.GONE
    views.promoCodeBottomSheetDeleteButton.visibility = View.VISIBLE
    views.promoCodeBottomSheetReplaceButton.visibility = View.VISIBLE
  }

  private fun hideAll() {
    hideDefaultScreen()
    hideButtons()
    hideLoading()
  }

  private fun hideDefaultScreen() {
    views.promoCodeBottomSheetTitle.visibility = View.GONE
    views.promoCodeBottomSheetActiveCheckmark.visibility = View.GONE
    views.promoCodeBottomSheetString.visibility = View.GONE
  }

  private fun hideButtons() {
    views.promoCodeBottomSheetSubmitButton.visibility = View.GONE
    views.promoCodeBottomSheetReplaceButton.visibility = View.GONE
    views.promoCodeBottomSheetDeleteButton.visibility = View.GONE
  }

  private fun hideLoading() {
    views.promoCodeBottomSheetSystemView.visibility = View.GONE
  }
}