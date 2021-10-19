package com.asfoundation.wallet.eskills_withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentWithdrawBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.eskills_withdraw.repository.WithdrawAvailableAmount
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import java.math.BigDecimal
import javax.inject.Inject

class WithdrawFragment : BasePageViewFragment(),
    SingleStateFragment<WithdrawState, WithdrawSideEffect> {

  @Inject
  lateinit var withdrawViewModelFactory: WithdrawViewModelFactory

  @Inject
  lateinit var navigator: WithdrawNavigator

  private val viewModel: WithdrawViewModel by viewModels { withdrawViewModelFactory }
  private val views by viewBinding(FragmentWithdrawBinding::bind)

  companion object {
    fun newInstance() = WithdrawFragment()
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_withdraw, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setOnClickListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun setOnClickListeners() {
    views.layoutWithdrawEntry.withdrawButton.setOnClickListener { withdrawToFiat() }
    views.layoutWithdrawEntry.cancelButton.setOnClickListener { navigator.navigateBack() }
    views.layoutWithdrawError.tryAgainButton.setOnClickListener { withdrawToFiat() }
    views.layoutWithdrawError.laterButton.setOnClickListener { navigator.navigateBack() }
    views.layoutWithdrawSuccess.gotItButton.setOnClickListener { navigator.navigateBack() }
  }

  private fun withdrawToFiat() {
    val paypalEmail: String = views.layoutWithdrawEntry.paypalEmail.text.toString()
    if (paypalEmail.isEmpty()) {
      views.layoutWithdrawEntry.paypalEmail.error = getString(R.string.error_field_required)
    }

    val amount: String = views.layoutWithdrawEntry.amount.text.toString()
    if (amount.isEmpty()) {
      views.layoutWithdrawEntry.amount.error = getString(R.string.error_field_required)
    }

    if (paypalEmail.isNotEmpty() && amount.isNotEmpty()) {
      viewModel.withdrawToFiat(paypalEmail, BigDecimal(amount))
    }
  }

  override fun onSideEffect(sideEffect: WithdrawSideEffect) = Unit

  override fun onStateChanged(state: WithdrawState) {
    handleAmountChangedState(state.availableAmount)
    handleWithdrawChangedState(state.withdrawResultAsync)
  }

  private fun handleAmountChangedState(asyncAvailableAmount: Async<WithdrawAvailableAmount>) {
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

  private fun setWithdrawAvailableAmount(availableAmount: WithdrawAvailableAmount) {
    views.layoutWithdrawEntry.availableAmountSkeleton.cancelAnimation()
    views.layoutWithdrawEntry.availableAmountSkeleton.visibility = View.GONE
    views.layoutWithdrawEntry.withdrawAvailableAmount.visibility = View.VISIBLE
    views.layoutWithdrawEntry.withdrawAvailableAmount.text = getString(
        R.string.e_skills_withdraw_max_amount_part_2, availableAmount.amount)
  }

  private fun handleWithdrawChangedState(asyncWithdrawResult: Async<WithdrawResult>) {
    when (asyncWithdrawResult) {
      is Async.Uninitialized -> {
        showEntryLayout()
      }
      is Async.Loading -> {
        if (asyncWithdrawResult.value == null) {
          showLoadingLayout()
        }
      }
      is Async.Fail -> {
        handleErrorState(WithdrawResult.Status.ERROR)
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
  }

  private fun handleSuccessState(withdrawResult: WithdrawResult) {
    // success here can be badly interpreted. this means the operation was successful,
    // but doesn't mean the operation result is
    when (withdrawResult.status) {
      WithdrawResult.Status.SUCCESS -> {
        views.layoutWithdrawSuccess.withdrawSuccessMessage.text =
            getString(R.string.e_skills_withdraw_started, withdrawResult.amount)
        showSuccessLayout()
      }
      else -> handleErrorState(withdrawResult.status)
    }
  }

  private fun showSuccessLayout() {
    views.layoutWithdrawEntry.root.visibility = View.GONE
    views.layoutWithdrawLoading.root.visibility = View.GONE
    views.layoutWithdrawError.root.visibility = View.GONE
    views.layoutWithdrawSuccess.root.visibility = View.VISIBLE
  }

  private fun handleErrorState(withdrawStatus: WithdrawResult.Status) {
    when (withdrawStatus) {
      WithdrawResult.Status.NOT_ENOUGH_EARNING -> {
        views.layoutWithdrawError.withdrawErrorMessage.text =
            getString(R.string.e_skills_withdraw_not_enough_earnings_error_message)
      }
      WithdrawResult.Status.NOT_ENOUGH_BALANCE -> {
        views.layoutWithdrawError.withdrawErrorMessage.text =
            getString(R.string.e_skills_withdraw_not_enough_balance_error_message)
      }
      WithdrawResult.Status.NO_NETWORK -> {
        views.layoutWithdrawError.withdrawErrorMessage.text =
            getString(R.string.activity_iab_no_network_message)
      }
      WithdrawResult.Status.INVALID_EMAIL -> {
        views.layoutWithdrawError.withdrawErrorMessage.text =
            getString(R.string.e_skills_withdraw_invalid_email_error_message)
      }
      WithdrawResult.Status.MIN_AMOUNT_REQUIRED -> {
        views.layoutWithdrawError.withdrawErrorMessage.text =
            getString(R.string.e_skills_withdraw_minimum_amount_error_message)
      }
      else -> return
    }
    showErrorLayout()
  }

  private fun showErrorLayout() {
    views.layoutWithdrawEntry.root.visibility = View.GONE
    views.layoutWithdrawLoading.root.visibility = View.GONE
    views.layoutWithdrawError.root.visibility = View.VISIBLE
    views.layoutWithdrawSuccess.root.visibility = View.GONE
  }
}
