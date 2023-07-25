package com.asfoundation.wallet.eskills.withdraw


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.ViewState
import com.asf.wallet.R
import com.asf.wallet.databinding.WithdrawSuccessBottomSheetLayoutBinding
import com.asfoundation.wallet.wallet_reward.RewardSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WithdrawSuccessBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ViewState, SideEffect> {


  @Inject
  lateinit var navigator: WithdrawBottomSheetNavigator

  private val views by viewBinding(WithdrawSuccessBottomSheetLayoutBinding::bind)

  private val rewardSharedViewModel: RewardSharedViewModel by activityViewModels()

  companion object {

    private const val AMOUNT = "amount"

    @JvmStatic
    fun newInstance(amount: String): WithdrawSuccessBottomSheetFragment {
      return WithdrawSuccessBottomSheetFragment()
        .apply {
          arguments = Bundle().apply {
            putSerializable(AMOUNT, amount)
          }
        }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = WithdrawSuccessBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    showSuccess(requireArguments().getString(AMOUNT) as String)
    views.withdrawBottomSheetSuccessGotItButton.setOnClickListener {
      rewardSharedViewModel.onBottomSheetDismissed()
      navigator.navigateBack()
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(views.root.parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeDraggable
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit

  private fun showSuccess(amount: String) {
    views.withdrawBottomSheetSuccessImage.visibility = View.VISIBLE
    views.withdrawBottomSheetSuccessTitle.text =
      this.getString(
        R.string.e_skills_withdraw_started_title,
        amount
      )
  }
}
