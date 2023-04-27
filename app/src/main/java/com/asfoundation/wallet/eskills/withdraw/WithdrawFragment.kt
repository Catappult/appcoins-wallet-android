package com.asfoundation.wallet.eskills.withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentWithdrawBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.eskills.withdraw.domain.FailedWithdraw
import com.asfoundation.wallet.eskills.withdraw.domain.SuccessfulWithdraw
import com.asfoundation.wallet.eskills.withdraw.domain.WithdrawResult
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.intercom.android.sdk.utilities.KeyboardUtils
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class WithdrawFragment : BasePageViewFragment(),
  SingleStateFragment<WithdrawState, WithdrawSideEffect> {


  @Inject
  lateinit var navigator: WithdrawNavigator

  private val viewModel: WithdrawViewModel by viewModels()
  private val views by viewBinding(FragmentWithdrawBinding::bind)

  companion object {
    fun newInstance() = WithdrawFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = FragmentWithdrawBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setOnClickListeners()
    views.layoutWithdrawEntry.paypalEmail.doOnTextChanged { _, _, _, _ ->
      hideErrorMessages()
    }
    views.layoutWithdrawEntry.amount.doOnTextChanged { _, _, _, _ ->
      hideErrorMessages()
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun setOnClickListeners() {
    views.layoutWithdrawEntry.withdrawButton.setOnClickListener { withdrawToFiat() }
    views.layoutWithdrawEntry.cancelButton.setOnClickListener { navigator.navigateBack() }
    views.layoutWithdrawError.tryAgainButton.setOnClickListener { showEntryLayout() }
    views.layoutWithdrawError.laterButton.setOnClickListener { navigator.navigateBack() }
    views.layoutWithdrawSuccess.gotItButton.setOnClickListener { navigator.navigateBack() }
  }

  private fun withdrawToFiat() {
    val paypalEmail: String = views.layoutWithdrawEntry.paypalEmail.text.toString()
    val amount: String = views.layoutWithdrawEntry.amount.text.toString()
    if (paypalEmail.isEmpty()) {
      views.layoutWithdrawEntry.paypalEmail.error = getString(R.string.error_field_required)
      return
    }
    if (amount.isEmpty()) {
      views.layoutWithdrawEntry.amount.error = getString(R.string.error_field_required)
      return
    }
    if (amount.toFloatOrNull() ?: 0F <= 0F) {
      // the minimum withdraw amount (server side) is required here
      viewModel.withdrawToFiat(paypalEmail, BigDecimal("0.00001"))
      return
    }

    viewModel.withdrawToFiat(paypalEmail, BigDecimal(amount))
  }

  override fun onSideEffect(sideEffect: WithdrawSideEffect) = Unit

  override fun onStateChanged(state: WithdrawState) {
    handleUserEmailChangedState(state.userEmail)
    handleAmountChangedState(state.availableAmountAsync)
    handleWithdrawChangedState(state.withdrawResultAsync)
  }

  private fun handleUserEmailChangedState(userEmail: String) {
    views.layoutWithdrawEntry.paypalEmail.setText(userEmail)
  }

  private fun handleAmountChangedState(asyncAvailableAmount: Async<BigDecimal>) {
    when (asyncAvailableAmount) {
      Async.Uninitialized,
      is Async.Loading -> {
        if (asyncAvailableAmount.value == null) {
          views.layoutWithdrawEntry.availableAmountSkeleton.playAnimation()
          views.layoutWithdrawEntry.availableAmountSkeleton.visibility = View.VISIBLE
        }
      }
      is Async.Fail -> {
      }
      is Async.Success -> {
        setWithdrawAvailableAmount(asyncAvailableAmount())
      }
    }
  }

  private fun setWithdrawAvailableAmount(availableAmount: BigDecimal) {
    views.layoutWithdrawEntry.availableAmountSkeleton.cancelAnimation()
    views.layoutWithdrawEntry.availableAmountSkeleton.visibility = View.GONE
    views.layoutWithdrawEntry.withdrawAvailableAmount.visibility = View.VISIBLE
    views.layoutWithdrawEntry.withdrawAvailableAmount.text = getString(
      R.string.e_skills_withdraw_max_amount_part_2, availableAmount
    )
  }

  private fun handleWithdrawChangedState(asyncWithdrawResult: Async<WithdrawResult>) {
    when (asyncWithdrawResult) {
      is Async.Uninitialized -> {
        showEntryLayout()
      }
      is Async.Loading -> {
        showLoadingLayout()
      }
      is Async.Fail -> {
        handleErrorState(FailedWithdraw.GenericError(asyncWithdrawResult.error.toString()))
      }
      is Async.Success -> {
        handleSuccessState(asyncWithdrawResult())
      }
    }
  }

  private fun showEntryLayout() {
    views.layoutWithdrawEntry.root.visibility = View.VISIBLE
    views.layoutWithdrawLoading.root.visibility = View.GONE
    views.layoutWithdrawError.root.visibility = View.GONE
    views.layoutWithdrawSuccess.root.visibility = View.GONE
  }

  private fun showLoadingLayout() {
    views.layoutWithdrawEntry.root.visibility = View.GONE
    views.layoutWithdrawLoading.root.visibility = View.VISIBLE
    views.layoutWithdrawError.root.visibility = View.GONE
    views.layoutWithdrawSuccess.root.visibility = View.GONE
    hideErrorMessages()
  }

  private fun hideErrorMessages() {
    views.layoutWithdrawEntry.amountErrorText.visibility = View.GONE
    views.layoutWithdrawEntry.amountTextLayout.setBackgroundResource(
      R.drawable.rectangle_outline_grey_radius_8dp
    )
    views.layoutWithdrawEntry.emailErrorText.visibility = View.GONE
    views.layoutWithdrawEntry.emailTextLayout.setBackgroundResource(
      R.drawable.rectangle_outline_grey_radius_8dp
    )
  }

  private fun handleSuccessState(withdrawResult: WithdrawResult) {
    // success here can be badly interpreted. this means the operation was successful,
    // but doesn't mean the operation result is
    when (withdrawResult) {
      is SuccessfulWithdraw -> {
        views.layoutWithdrawSuccess.withdrawSuccessMessage.text =
          getString(R.string.e_skills_withdraw_started, withdrawResult.amount)
        showSuccessLayout()
      }
      else -> handleErrorState(withdrawResult)
    }
  }

  private fun showSuccessLayout() {
    KeyboardUtils.hideKeyboard(view)
    views.layoutWithdrawEntry.root.visibility = View.GONE
    views.layoutWithdrawLoading.root.visibility = View.GONE
    views.layoutWithdrawError.root.visibility = View.GONE
    views.layoutWithdrawSuccess.root.visibility = View.VISIBLE
  }

  private fun handleErrorState(withdrawResult: WithdrawResult) {
    when (withdrawResult) {
      is FailedWithdraw.NotEnoughEarningError -> {
        views.layoutWithdrawEntry.amountErrorText.text =
          getString(R.string.e_skills_withdraw_not_enough_earnings_error_message)
        showAmountErrorMessage()
      }
      is FailedWithdraw.NotEnoughBalanceError -> {
        views.layoutWithdrawEntry.amountErrorText.text =
          getString(R.string.e_skills_withdraw_not_enough_balance_error_message)
        showAmountErrorMessage()
      }
      is FailedWithdraw.MinAmountRequiredError -> {
        views.layoutWithdrawEntry.amountErrorText.text =
          getString(
            R.string.e_skills_withdraw_minimum_amount_error_message,
            withdrawResult.amount
          )
        showAmountErrorMessage()
      }
      is FailedWithdraw.InvalidEmailError -> {
        views.layoutWithdrawEntry.emailErrorText.text =
          getString(R.string.e_skills_withdraw_invalid_email_error_message)
        showEmailErrorMessage()
      }
      is FailedWithdraw.NoNetworkError -> {
        views.layoutWithdrawError.withdrawErrorMessage.text =
          getString(R.string.activity_iab_no_network_message)
        showErrorLayout()
      }
      is FailedWithdraw.GenericError -> showErrorLayout()
      else -> return
    }
  }

  private fun showAmountErrorMessage() {
    views.layoutWithdrawEntry.amountErrorText.visibility = View.VISIBLE
    views.layoutWithdrawEntry.amountTextLayout.setBackgroundResource(
      R.drawable.rectangle_outline_red_radius_8dp
    )
    showEntryLayout()
  }

  private fun showEmailErrorMessage() {
    views.layoutWithdrawEntry.emailErrorText.visibility = View.VISIBLE
    views.layoutWithdrawEntry.emailTextLayout.setBackgroundResource(
      R.drawable.rectangle_outline_red_radius_8dp
    )
    showEntryLayout()
  }

  private fun showErrorLayout() {
    KeyboardUtils.hideKeyboard(view)
    views.layoutWithdrawEntry.root.visibility = View.GONE
    views.layoutWithdrawLoading.root.visibility = View.GONE
    views.layoutWithdrawError.root.visibility = View.VISIBLE
    views.layoutWithdrawSuccess.root.visibility = View.GONE
  }
}
