package com.asfoundation.wallet.redeem_gift.bottom_sheet

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
import com.asf.wallet.databinding.SettingsRedeemGiftBottomSheetLayoutBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.util.KeyboardUtils
import com.asfoundation.wallet.util.setReadOnly
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RedeemGiftBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<RedeemGiftBottomSheetState, RedeemGiftBottomSheetSideEffect> {


  @Inject
  lateinit var navigator: RedeemGiftBottomSheetNavigator

  private val viewModel: RedeemGiftBottomSheetViewModel by viewModels()
  private val views by viewBinding(SettingsRedeemGiftBottomSheetLayoutBinding::bind)

  companion object {
    @JvmStatic
    fun newInstance(): RedeemGiftBottomSheetFragment {
      return RedeemGiftBottomSheetFragment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.settings_redeem_gift_bottom_sheet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    views.redeemGiftBottomSheetSubmitButton.setOnClickListener {
      viewModel.submitClick(views.redeemGiftBottomSheetString.text.toString())
    }
    views.redeemGiftBottomSheetSuccessGotItButton.setOnClickListener {
      viewModel.successGotItClick()
    }

    views.redeemGiftBottomSheetString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.redeemGiftBottomSheetSubmitButton.isEnabled = s.isNotEmpty()
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

  override fun onStateChanged(state: RedeemGiftBottomSheetState) {
    when (val clickAsync = state.submitClickAsync) {
      is Async.Uninitialized -> redeemGift(state.redeemGiftAsync, state.shouldShowDefault)
      is Async.Loading -> {
        if (clickAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
        showErrorMessage()
      }
      is Async.Success -> {
        state.redeemGiftAsync.value?.let { redeemGift -> showSuccess(redeemGift) }
      }
    }
  }

  override fun onSideEffect(sideEffect: RedeemGiftBottomSheetSideEffect) {
    when (sideEffect) {
      is RedeemGiftBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  fun redeemGift(redeemGiftAsync: Async<GiftCode>) {
    when (redeemGiftAsync) {
      is Async.Uninitialized,
      is Async.Loading -> {
        showDefaultScreen()
      }
      is Async.Fail -> {
        showErrorMessage()
      }
      is Async.Success -> {
        showDefaultScreen()
      }
    }
  }

  private fun showErrorMessage() {
    hideAll()
    views.redeemGiftBottomSheetString.setReadOnly(false, InputType.TYPE_CLASS_TEXT)
    views.redeemGiftBottomSheetTitle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetTextRectangle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetTextRectangle.setBackgroundResource(R.drawable.rectangle_outline_red)
    views.redeemGiftBottomSheetErrorMessage.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSubmitButton.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSubmitButton.isEnabled = false
  }

  private fun showLoading() {
    hideAll()
    views.redeemGiftBottomSheetSystemView.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSystemView.showProgress(true)
  }

  private fun showDefaultScreen() {
    hideAll()
    views.redeemGiftBottomSheetString.setReadOnly(false, InputType.TYPE_CLASS_TEXT)
    views.redeemGiftBottomSheetTitle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetTextRectangle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetTextRectangle.setBackgroundResource(
      R.drawable.rectangle_text_redeem_gift)
    views.redeemGiftBottomSheetSubmitButton.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSubmitButton.isEnabled = false
  }

  @SuppressLint("StringFormatMatches")
  private fun showSuccess(redeemGift: GiftCode) {
    hideAll()
    KeyboardUtils.hideKeyboard(view)
    views.redeemGiftBottomSheetSuccessAnimation.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSuccessAnimation.setAnimation(R.raw.success_animation)
    views.redeemGiftBottomSheetSuccessAnimation.setAnimation(R.raw.success_animation)
    views.redeemGiftBottomSheetSuccessAnimation.repeatCount = 0
    views.redeemGiftBottomSheetSuccessAnimation.playAnimation()
    views.redeemGiftBottomSheetSuccessTitle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSuccessSubtitle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSuccessGotItButton.visibility = View.VISIBLE

  }

  private fun hideAll() {
    hideDefaultScreen()
    hideButtons()
    hideLoading()
    hideErrorMessage()
    hideSuccessScreen()
  }

  private fun hideDefaultScreen() {
    views.redeemGiftBottomSheetTitle.visibility = View.GONE
    views.redeemGiftBottomSheetTextRectangle.visibility = View.GONE
    views.redeemGiftBottomSheetActiveCheckmark.visibility = View.GONE
  }

  private fun hideButtons() {
    views.redeemGiftBottomSheetSubmitButton.visibility = View.GONE
    views.redeemGiftBottomSheetReplaceButton.visibility = View.GONE
    views.redeemGiftBottomSheetDeleteButton.visibility = View.GONE
  }

  private fun hideLoading() {
    views.redeemGiftBottomSheetSystemView.visibility = View.GONE
  }

  private fun hideErrorMessage() {
    views.redeemGiftBottomSheetErrorMessage.visibility = View.GONE
  }

  private fun hideSuccessScreen() {
    views.redeemGiftBottomSheetSuccessAnimation.visibility = View.GONE
    views.redeemGiftBottomSheetSuccessTitle.visibility = View.GONE
    views.redeemGiftBottomSheetSuccessSubtitle.visibility = View.GONE
    views.redeemGiftBottomSheetSuccessGotItButton.visibility = View.GONE
  }
}