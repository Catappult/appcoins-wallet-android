package com.asfoundation.wallet.promo_code.bottom_sheet.entry


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.SettingsPromoCodeBottomSheetLayoutBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.feature.promocode.data.FailedPromoCode
import com.appcoins.wallet.feature.promocode.data.PromoCodeResult
import com.appcoins.wallet.feature.promocode.data.SuccessfulPromoCode
import com.asfoundation.wallet.promo_code.bottom_sheet.PromoCodeBottomSheetNavigator
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.ui.widgets.WalletTextFieldView
import com.asfoundation.wallet.wallet_reward.RewardSharedViewModel
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

  private val rewardSharedViewModel: RewardSharedViewModel by activityViewModels()

  companion object {
    @JvmStatic
    fun newInstance(): PromoCodeBottomSheetFragment {
      return PromoCodeBottomSheetFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = SettingsPromoCodeBottomSheetLayoutBinding.inflate(inflater).root

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
    return R.style.AppBottomSheetDialogThemeDraggable
  }

  private fun setListeners() {
    views.promoCodeBottomSheetSubmitButton.setOnClickListener {
      viewModel.submitClick(views.promoCodeBottomSheetString.getText().trim())
    }
    views.promoCodeBottomSheetReplaceButton.setOnClickListener {
      viewModel.replaceClick()
    }
    views.promoCodeBottomSheetDeleteButton.setOnClickListener {
      rewardSharedViewModel.onBottomSheetDismissed()
      viewModel.deleteClick()
    }

    views.promoCodeBottomSheetString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.promoCodeBottomSheetSubmitButton.isEnabled = s.isNotEmpty()
      }

      override fun afterTextChanged(s: Editable) = Unit
    })
  }

  override fun onStateChanged(state: PromoCodeBottomSheetState) {
    when (val clickAsync = state.submitPromoCodeAsync) {
      is Async.Uninitialized -> initializePromoCode(
        state.storedPromoCodeAsync,
        state.shouldShowDefault
      )
      is Async.Loading -> {
        if (clickAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
        handleErrorState(FailedPromoCode.InvalidCode(clickAsync.error.throwable))
      }
      is Async.Success -> {
        handleClickSuccessState(state.submitPromoCodeAsync.value)
      }
    }
  }

  override fun onSideEffect(sideEffect: PromoCodeBottomSheetSideEffect) {
    when (sideEffect) {
      is PromoCodeBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  fun initializePromoCode(
    storedPromoCodeAsync: Async<PromoCodeResult>,
    shouldShowDefault: Boolean
  ) {
    when (storedPromoCodeAsync) {
      is Async.Uninitialized,
      is Async.Loading -> {
        showDefaultScreen()
      }
      is Async.Fail -> {
        if (storedPromoCodeAsync.value != null) {
          handleErrorState(FailedPromoCode.GenericError(storedPromoCodeAsync.error.throwable))
        }
      }
      is Async.Success -> {
        storedPromoCodeAsync.value?.let { handlePromoCodeSuccessState(it, shouldShowDefault) }
      }
    }
  }

  private fun handleClickSuccessState(promoCode: PromoCodeResult?) {
    when (promoCode) {
      is SuccessfulPromoCode -> {
        promoCode.promoCode.code?.let {
          if (viewModel.isFirstSuccess) {
            KeyboardUtils.hideKeyboard(view)
            navigator.navigateToSuccess(promoCode.promoCode)
            viewModel.isFirstSuccess = false
          }
        }
      }
      else -> handleErrorState(promoCode)
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
      else -> handleErrorState(null)
    }
  }

  private fun handleErrorState(promoCodeResult: PromoCodeResult?) {
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
    views.promocodeImage.visibility = View.VISIBLE
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promoCodeBottomSheetSystemView.showProgress(true)
  }

  private fun showDefaultScreen() {
    hideAll()
    views.promoCodeBottomSheetString.setType(WalletTextFieldView.Type.FILLED)
    views.promoCodeBottomSheetString.setColor(
      ContextCompat.getColor(
        requireContext(),
        R.color.styleguide_blue
      )
    )
    views.promoCodeBottomSheetString.visibility = View.VISIBLE
    views.promoCodeBottomSheetTitle.visibility = View.VISIBLE
    views.promocodeImage.visibility = View.VISIBLE
    views.promoCodeBottomSheetSubtitle.visibility = View.VISIBLE
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
    views.promocodeImage.visibility = View.VISIBLE
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
    views.promoCodeBottomSheetSubtitle.visibility = View.GONE
    views.promocodeImage.visibility = View.GONE
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