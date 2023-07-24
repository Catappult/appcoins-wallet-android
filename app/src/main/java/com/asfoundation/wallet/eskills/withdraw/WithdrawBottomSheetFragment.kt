package com.asfoundation.wallet.eskills.withdraw


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
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.ui.widgets.WalletTextFieldView
import com.asf.wallet.R
import com.asf.wallet.databinding.EskillsWithdrawBottomSheetLayoutBinding
import com.asfoundation.wallet.wallet_reward.RewardSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

@AndroidEntryPoint
class WithdrawBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<WithdrawBottomSheetState, WithdrawBottomSheetSideEffect> {

//  @Inject  //TODO
//  lateinit var navigator: WithdrawBottomSheetNavigator

  private val viewModel: WithdrawBottomSheetViewModel by viewModels()
  private val views by viewBinding(EskillsWithdrawBottomSheetLayoutBinding::bind)

  private val rewardSharedViewModel: RewardSharedViewModel by activityViewModels()

  companion object {
    @JvmStatic
    fun newInstance(): WithdrawBottomSheetFragment {
      return WithdrawBottomSheetFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = EskillsWithdrawBottomSheetLayoutBinding.inflate(inflater).root

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
    views.eskillsBottomSheetSubmitButton.setOnClickListener {
      viewModel.submitClick(
        views.eskillsEmailString.getText().trim(),
        views.eskillsAmountString.getText().trim()
      )
    }

    views.eskillsEmailString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.eskillsBottomSheetSubmitButton.isEnabled = s.isNotEmpty() // TODO condition for enable
      }
      override fun afterTextChanged(s: Editable) = Unit
    })

    views.eskillsAmountString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        views.eskillsBottomSheetSubmitButton.isEnabled = s.isNotEmpty() // TODO condition for enable (amount + email)
      }
      override fun afterTextChanged(s: Editable) = Unit
    })
  }

  override fun onStateChanged(state: WithdrawBottomSheetState) {
    when (val clickAsync = state.submitWithdrawAsync) {
      is Async.Uninitialized -> initializeWithdraw(
        state.withdrawAmountAsync
      )
      is Async.Loading -> {
        if (clickAsync.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
//        handleErrorState(FailedWithdraw.InvalidCode(clickAsync.error.throwable))  // TODO
      }
      is Async.Success -> {
        handleClickSuccessState(state.submitWithdrawAsync.value)
      }
    }
  }

  override fun onSideEffect(sideEffect: WithdrawBottomSheetSideEffect) {
    when (sideEffect) {
      is WithdrawBottomSheetSideEffect.NavigateBack -> {
//        navigator.navigateBack()
      }
    }
  }

  fun initializeWithdraw(
    withdrawAmountAsync: Async<BigDecimal>
  ) {
    when (withdrawAmountAsync) {
      is Async.Uninitialized,
      is Async.Loading -> {
        showDefaultScreen(withdrawAmountAsync.value?.toFloat() ?: 0F)
      }
      is Async.Fail -> {
        if (withdrawAmountAsync.value != null) {
          handleErrorState(
            FailedWithdrawAmount.GenericError(withdrawAmountAsync.error.throwable),
            withdrawAmountAsync.value?.toFloat() ?: 0F
          )
        }
      }
      is Async.Success -> {
        withdrawAmountAsync.value?.let {
          showDefaultScreen(it.toFloat())
        }
      }
    }
  }

  private fun handleClickSuccessState(withdraw: WithdrawAmountResult?) { // TODO
//    when (withdraw) {   //TODO
//      is SuccessfulWithdraw -> {
//        withdraw.withdraw.code?.let {
//          if (viewModel.isFirstSuccess) {
//            KeyboardUtils.hideKeyboard(view)
//            navigator.navigateToSuccess(withdraw.withdraw)
//            viewModel.isFirstSuccess = false
//          }
//        }
//      }
//      else -> handleErrorState(withdraw)
//    }
  }

  private fun handleErrorState(withdrawAmountResult: WithdrawAmountResult?, withdrawAmount: Float) {
    showDefaultScreen(withdrawAmount)
    views.eskillsBottomSheetSubmitButton.isEnabled = false
    when (withdrawAmountResult) {
      is FailedWithdrawAmount.NoBalanceCode -> {  //TODO errors
        views.eskillsAmountString.setError(getString(R.string.promo_code_view_error))
      }
      is FailedWithdrawAmount.GenericError -> {
        views.eskillsAmountString.setError(getString(R.string.promo_code_error_invalid_user))
      }
      else -> return
    }
  }

  private fun showLoading() {
    hideAll()
    views.eskillsBottomSheetSystemView.visibility = View.VISIBLE
    views.eskillsImage.visibility = View.VISIBLE
    views.eskillsBottomSheetTitle.visibility = View.VISIBLE
    views.eskillsBottomSheetSystemView.showProgress(true)
  }

  private fun showDefaultScreen(withdrawAmount: Float) {
    hideAll()

    views.eskillsAmountText.text = getString(
      R.string.e_skills_withdraw_max_amount_part_2,
      withdrawAmount
      )
    views.eskillsAmountText.visibility = View.VISIBLE

    views.eskillsEmailString.setType(WalletTextFieldView.Type.FILLED)
    views.eskillsEmailString.setColor(
      ContextCompat.getColor(
        requireContext(),
        R.color.styleguide_blue
      )
    )
    views.eskillsEmailString.visibility = View.VISIBLE

    views.eskillsAmountString.setType(WalletTextFieldView.Type.FILLED)
    views.eskillsAmountString.setColor(
      ContextCompat.getColor(
        requireContext(),
        R.color.styleguide_blue
      )
    )
    views.eskillsAmountString.visibility = View.VISIBLE

    views.eskillsBottomSheetTitle.visibility = View.VISIBLE
    views.eskillsImage.visibility = View.VISIBLE
    views.eskillsEmailSubtitle.visibility = View.VISIBLE
    views.eskillsAmountSubtitle.visibility = View.VISIBLE
    views.eskillsBottomSheetSubmitButton.visibility = View.VISIBLE
    views.eskillsBottomSheetSubmitButton.isEnabled = false
  }

  private fun hideAll() {
    hideDefaultScreen()
    hideButtons()
    hideLoading()
  }

  private fun hideDefaultScreen() {
    views.eskillsBottomSheetTitle.visibility = View.GONE
    views.eskillsAmountText.visibility = View.GONE
    views.eskillsEmailString.visibility = View.GONE
    views.eskillsAmountString.visibility = View.GONE
    views.eskillsEmailSubtitle.visibility = View.GONE
    views.eskillsAmountSubtitle.visibility = View.GONE
    views.eskillsImage.visibility = View.GONE
  }

  private fun hideButtons() {
    views.eskillsBottomSheetSubmitButton.visibility = View.GONE
  }

  private fun hideLoading() {
    views.eskillsBottomSheetSystemView.visibility = View.GONE
  }
}