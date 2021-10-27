package com.asfoundation.wallet.promo_code.bottom_sheet


import android.os.Bundle
import android.text.Editable
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
import com.asfoundation.wallet.promo_code.repository.PromoCodeEntity
import com.asfoundation.wallet.util.KeyboardUtils
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
      viewModel.submitClick(views.promoCodeBottomSheetString.text.toString())
    }
    views.promoCodeBottomSheetDeleteButton.setOnClickListener { viewModel.deleteClick() }
    views.promoCodeBottomSheetSuccessGotItButton.setOnClickListener { viewModel.successGotItClick() }
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
    setPromoCode(state.promoCodeAsync)
    setSubmitClickState(state.submitClickAsync)
  }

  override fun onSideEffect(sideEffect: PromoCodeBottomSheetSideEffect) {
    when (sideEffect) {
      is PromoCodeBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  fun setPromoCode(promoCodeAsync: Async<PromoCodeEntity>) {
    when (promoCodeAsync) {
      is Async.Uninitialized,
      is Async.Loading -> {
        showDefaultView()
      }
      is Async.Fail -> {
      }
      is Async.Success -> {
        if (promoCodeAsync.value?.code != "") {
          promoCodeAsync.value?.code?.let { showSavedView(it) }
        }
      }
    }
  }

  fun setSubmitClickState(clickAsync: Async<Unit>) {
    when (clickAsync) {
      is Async.Uninitialized -> {
      }
      is Async.Loading -> {
        if (clickAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
      }
      is Async.Success -> {
        showSuccess()
      }
    }
  }

  fun showLoading() {
    hideAll()
    views.promoCodeBottomSheetSystemView.visibility = View.VISIBLE
    views.promoCodeBottomSheetSystemView.showProgress(true)
  }

  fun showDefaultView() {
    hideAll()
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetTextRectangle.visibility = View.VISIBLE
    views.promoCodeBottomSheetSubmitButton.visibility = View.VISIBLE
  }

  fun showSavedView(promoCodeString: String) {
    hideAll()
    views.promoCodeBottomSheetString.text = Editable.Factory.getInstance()
        .newEditable(promoCodeString)
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetTextRectangle.visibility = View.VISIBLE
    views.promoCodeBottomSheetActiveCheckmark.visibility = View.VISIBLE
    views.promoCodeBottomSheetDeleteButton.visibility = View.VISIBLE
    views.promoCodeBottomSheetReplaceButton.visibility = View.VISIBLE
  }

  fun showSuccess() {
    hideAll()
    KeyboardUtils.hideKeyboard(view)
    views.promoCodeBottomSheetSuccessSymbol.visibility = View.VISIBLE
    views.promoCodeBottomSheetSuccessTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetSuccessSubtitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetSuccessGotItButton.visibility = View.VISIBLE

  }

  fun hideAll() {
    hideDefaultView()
    hideButtons()
    hideLoading()
    hideSuccessView()
  }

  private fun hideDefaultView() {
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

  private fun hideSuccessView() {
    views.promoCodeBottomSheetSuccessSymbol.visibility = View.GONE
    views.promoCodeBottomSheetSuccessTitle.visibility = View.GONE
    views.promoCodeBottomSheetSuccessSubtitle.visibility = View.GONE
    views.promoCodeBottomSheetSuccessGotItButton.visibility = View.GONE
  }
}