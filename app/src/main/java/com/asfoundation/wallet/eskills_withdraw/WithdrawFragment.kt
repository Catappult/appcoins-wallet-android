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
    views.withdrawButton.setOnClickListener {
      viewModel.withdrawToFiat(
          views.paypalEmail.text.toString(),
          BigDecimal(views.amount.text.toString())
      )
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
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
        }
      }
      is Async.Fail -> {}
      is Async.Success -> {
        setWithdrawAvailableAmount(asyncAvailableAmount())
      }
    }
  }

  private fun setWithdrawAvailableAmount(availableAmount: WithdrawAvailableAmount) {
    val textAmount = resources.getString(R.string.e_skills_withdraw_max_amount, availableAmount.amount)
    views.withdrawAvailableAmount.text = textAmount
  }

  private fun handleWithdrawChangedState(asyncWithdrawResult: Async<WithdrawResult>) {
    when (asyncWithdrawResult) {
      is Async.Uninitialized -> {}
      is Async.Loading -> {
        if (asyncWithdrawResult.value == null) {
          showLoading()
        }
      }
      is Async.Fail -> {
        hideLoading()
        handleErrorState()
      }
      is Async.Success -> {
        hideLoading()
        handleSuccessState(asyncWithdrawResult())
      }
    }
  }

  private fun showLoading() {
    views.withdrawButton.isEnabled = false
    views.loadingLayout.visibility = View.VISIBLE
  }

  private fun hideLoading() {
    views.withdrawButton.isEnabled = true
    views.loadingLayout.visibility = View.GONE
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
      WithdrawResult.Status.SUCCESS -> showWithdrawSuccessMessage()
      WithdrawResult.Status.NOT_ENOUGH_EARNING -> showNotEnoughEarningsBalanceError()
      WithdrawResult.Status.NOT_ENOUGH_BALANCE -> showNotEnoughBalanceError()
      WithdrawResult.Status.NO_NETWORK -> showNoNetworkError()
      WithdrawResult.Status.INVALID_EMAIL -> showInvalidEmailError()
    }
  }

  private fun showWithdrawSuccessMessage() {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(R.string.transaction_status_success)
        .setPositiveButton(R.string.ok) { dialog, _ ->
          dialog.dismiss()
          activity?.onBackPressed()
        }
        .show()
  }

  private fun showNotEnoughEarningsBalanceError() {
    views.amount.error = getString(R.string.e_skills_withdraw_not_enough_earnings_error_message)
  }

  private fun showNotEnoughBalanceError() {
    views.amount.error = getString(R.string.e_skills_withdraw_not_enough_balance_error_message)
  }

  private fun showNoNetworkError() {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(R.string.activity_iab_no_network_message)
        .setPositiveButton(
            R.string.ok
        ) { dialog, _ -> dialog.dismiss() }
        .show()
  }

  private fun showInvalidEmailError() {
    views.paypalEmail.error = getString(R.string.e_skills_withdraw_invalid_email_error_message)
  }
}
