package com.asfoundation.wallet.eskills_withdraw

import android.app.AlertDialog
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
    views.layoutWithdrawEntry.withdrawButton.setOnClickListener { withdrawToFiat() }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
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
      }
      is Async.Loading -> {
        if (asyncWithdrawResult.value == null) {
          views.layoutWithdrawEntry.root.visibility = View.GONE
          views.layoutWithdrawLoading.root.visibility = View.VISIBLE
        }
      }
      is Async.Fail -> {
        views.layoutWithdrawLoading.root.visibility = View.GONE
        handleErrorState()
      }
      is Async.Success -> {
        views.layoutWithdrawLoading.root.visibility = View.GONE
        handleSuccessState(asyncWithdrawResult())
      }
    }
  }

  private fun handleErrorState() {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(R.string.unknown_error)
        .setPositiveButton(R.string.ok) { dialog, _ ->
          dialog.dismiss()
        }
        .show()
  }

  private fun handleSuccessState(withdrawResult: WithdrawResult) {
    when (withdrawResult.status) {
      WithdrawResult.Status.SUCCESS -> showWithdrawSuccess()
      else -> showWithdrawError(withdrawResult.status)
    }
  }

  private fun showWithdrawSuccess() {
    views.layoutWithdrawSuccess.root.visibility = View.VISIBLE
  }

  private fun showWithdrawError(withdrawStatus: WithdrawResult.Status) {
    views.layoutWithdrawError.root.visibility = View.VISIBLE
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
      else -> return
    }
  }
}
