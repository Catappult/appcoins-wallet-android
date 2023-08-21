package com.asfoundation.wallet.redeem_gift.bottom_sheet

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
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.redeem_gift.repository.FailedRedeem
import com.asfoundation.wallet.redeem_gift.repository.SuccessfulRedeem
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.ui.common.setReadOnly
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
                            savedInstanceState: Bundle?): View = SettingsRedeemGiftBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    views.redeemGiftBottomSheetSubmitButton.setOnClickListener {
      viewModel.submitClick(views.redeemGiftBottomSheetString.text.toString().trim())
    }
    views.redeemGiftBottomSheetSuccessGotItButton.setOnClickListener {
      viewModel.successGotItClick()
    }
    views.redeemGiftBottomSheetErrorButton.setOnClickListener {
      showDefaultScreen()
    }

    views.redeemGiftBottomSheetString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.redeemGiftBottomSheetSubmitButton.isEnabled = s.isNotEmpty()
      }
      override fun afterTextChanged(s: Editable) = Unit
    })

    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    views.redeemGiftBottomSheetSubmitButton.isEnabled = false
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeDraggable
  }

  override fun onStateChanged(state: RedeemGiftBottomSheetState) {
    when (val clickAsync = state.submitRedeemAsync) {
      is Async.Uninitialized ->  {
      }
      is Async.Loading -> {
        if (clickAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
        showErrorMessage(FailedRedeem.GenericError(""))
      }
      is Async.Success -> {
        state.submitRedeemAsync.value?.let { redeemState ->
          if (redeemState is SuccessfulRedeem)
            showSuccess()
          else
            showErrorMessage(redeemState as? FailedRedeem ?: FailedRedeem.GenericError(""))
        }
      }
    }
  }

  override fun onSideEffect(sideEffect: RedeemGiftBottomSheetSideEffect) {
    when (sideEffect) {
      is RedeemGiftBottomSheetSideEffect.NavigateBack -> navigator.navigateBack()
    }
  }

  private fun showErrorMessage(error: FailedRedeem) {
    hideAll()
    KeyboardUtils.hideKeyboard(view)
    views.redeemGiftBottomSheetErrorImage.visibility = View.VISIBLE
    when(error) {
      FailedRedeem.OnlyNewUsersError ->{
        views.redeemGiftBottomSheetErrorTitle.text = getString(R.string.gift_card_error_new_users_title)
        views.redeemGiftBottomSheetErrorSubtitle.text = getString(R.string.gift_card_error_try_with_different_body)
      }
      FailedRedeem.AlreadyRedeemedError ->{
        views.redeemGiftBottomSheetErrorTitle.text = getString(R.string.gift_card_error_used_title)
        views.redeemGiftBottomSheetErrorSubtitle.text = getString(R.string.gift_card_error_try_with_different_body)
      }
      is FailedRedeem.GenericError ->{
        views.redeemGiftBottomSheetErrorTitle.text = getString(R.string.gift_card_error_general_title)
        views.redeemGiftBottomSheetErrorSubtitle.text = getString(R.string.gift_card_error_general_body)
      }
    }
    views.redeemGiftBottomSheetErrorTitle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetErrorSubtitle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetErrorButton.visibility = View.VISIBLE
  }

  private fun showLoading() {
    hideAll()
    views.redeemGiftBottomSheetLoadingAnimation.visibility = View.VISIBLE
  }

  private fun showDefaultScreen() {
    hideAll()
    views.redeemGiftBottomSheetString.setReadOnly(false, InputType.TYPE_CLASS_TEXT)
    views.redeemGiftBottomSheetTitle.visibility = View.VISIBLE
    views.giftcardImage.visibility = View.VISIBLE
    views.redeemGiftBottomSheetTextRectangle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSubmitButton.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSubmitButton.isEnabled = false
  }

  private fun showSuccess() {
    hideAll()
    KeyboardUtils.hideKeyboard(view)
    views.redeemGiftBottomSheetSuccessImage.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSuccessTitle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSuccessSubtitle.visibility = View.VISIBLE
    views.redeemGiftBottomSheetSuccessGotItButton.visibility = View.VISIBLE

  }

  private fun hideAll() {
    hideDefaultScreen()
    hideButtons()
    hideLoading()
    hideErrorScreen()
    hideSuccessScreen()
  }

  private fun hideDefaultScreen() {
    views.redeemGiftBottomSheetTitle.visibility = View.GONE
    views.giftcardImage.visibility = View.GONE
    views.redeemGiftBottomSheetTextRectangle.visibility = View.GONE
  }

  private fun hideButtons() {
    views.redeemGiftBottomSheetSubmitButton.visibility = View.GONE
  }

  private fun hideLoading() {
    views.redeemGiftBottomSheetLoadingAnimation.visibility = View.GONE
  }

  private fun hideErrorScreen() {
    views.redeemGiftBottomSheetErrorImage.visibility = View.GONE
    views.redeemGiftBottomSheetErrorTitle.visibility = View.GONE
    views.redeemGiftBottomSheetErrorSubtitle.visibility = View.GONE
    views.redeemGiftBottomSheetErrorButton.visibility = View.GONE
  }

  private fun hideSuccessScreen() {
    views.redeemGiftBottomSheetSuccessImage.visibility = View.GONE
    views.redeemGiftBottomSheetSuccessTitle.visibility = View.GONE
    views.redeemGiftBottomSheetSuccessSubtitle.visibility = View.GONE
    views.redeemGiftBottomSheetSuccessGotItButton.visibility = View.GONE
  }
}