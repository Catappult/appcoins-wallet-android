package com.asfoundation.wallet.referrals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.ReferralsLayoutBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class ReferralsFragment : BasePageViewFragment(), ReferralsView {

  private lateinit var presenter: ReferralsPresenter

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val binding by viewBinding(ReferralsLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = ReferralsPresenter(this, CompositeDisposable())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = ReferralsLayoutBinding.inflate(inflater).root

  override fun setupLayout() {
    val totalAvailable = completedInvites + available
    binding.friendsInvited.text = String.format("%d/%d", completedInvites, totalAvailable)
    binding.friendsInvited.visibility = VISIBLE
    binding.numberFriendsInvited.text = String.format("%d/%d", completedInvites, totalAvailable)
    binding.totalEarned.text = currency.plus(formatter.formatCurrency(getTotalEarned(),
        WalletCurrency.FIAT))
    binding.totalEarned.visibility = VISIBLE
    val individualEarn = currency.plus(formatter.formatCurrency(amount, WalletCurrency.FIAT))
    val totalEarn =
        currency.plus(formatter.formatCurrency(amount.multiply(BigDecimal(totalAvailable)),
            WalletCurrency.FIAT))
    binding.referralExplanation.text =
        getString(R.string.referral_dropup_menu_requirements_body, individualEarn, totalEarn)
    binding.invitationsProgressBar.progress =
        ((100 / (completedInvites.toDouble() + available.toDouble())) * completedInvites).roundToInt()
    setFriendsAnimations(completedInvites, completedInvites + available)
  }

  override fun bottomSheetHeaderClick() = RxView.clicks(binding.bottomSheetHeader)

  override fun changeBottomSheetState() {
    val parentFragment = provideParentFragment()
    parentFragment?.changeBottomSheetState()
  }

  private fun provideParentFragment(): InviteFriendsFragment? {
    if (parentFragment !is InviteFriendsFragment) {
      return null
    }
    return parentFragment as InviteFriendsFragment
  }

  private fun getTotalEarned(): BigDecimal {
    return if (!isRedeemed) receivedAmount else {
      amount.multiply(BigDecimal(completedInvites))
    }
  }

  private fun setFriendsAnimations(invited: Int, totalInvitations: Int) {
    val friendsAnimation =
        arrayOf(binding.friendsList.friendAnimation1, binding.friendsList.friendAnimation2, binding.friendsList.friendAnimation3, binding.friendsList.friendAnimation4,
          binding.friendsList.friendAnimation5)

    for (animationIndex in friendsAnimation.indices) {
      if (animationIndex < invited) {
        friendsAnimation[animationIndex].setAnimation(R.raw.invited_user_animation)
        friendsAnimation[animationIndex].playAnimation()
      }
      if (animationIndex >= totalInvitations) {
        //If there are less animation icons than total invitations, remove extra icons
        friendsAnimation[animationIndex].visibility = GONE
      }
    }
  }

  private val receivedAmount: BigDecimal by lazy {
    if (requireArguments().containsKey(RECEIVED_AMOUNT)) {
      requireArguments().getSerializable(RECEIVED_AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Received amount not found")
    }
  }

  private val amount: BigDecimal by lazy {
    if (requireArguments().containsKey(AMOUNT)) {
      requireArguments().getSerializable(AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Amount not found")
    }
  }

  private val currency: String by lazy {
    if (requireArguments().containsKey(CURRENCY)) {
      requireArguments().getString(CURRENCY, "")
    } else {
      throw IllegalArgumentException("Currency not found")
    }
  }

  private val completedInvites: Int by lazy {
    if (requireArguments().containsKey(COMPLETED_INVITES)) {
      requireArguments().getInt(COMPLETED_INVITES)
    } else {
      throw IllegalArgumentException("Completed not found")
    }
  }

  private val available: Int by lazy {
    if (requireArguments().containsKey(AVAILABLE)) {
      requireArguments().getInt(AVAILABLE)
    } else {
      throw IllegalArgumentException("available not found")
    }
  }

  private val isRedeemed: Boolean by lazy {
    if (requireArguments().containsKey(IS_REDEEMED)) {
      requireArguments().getBoolean(IS_REDEEMED)
    } else {
      throw IllegalArgumentException("is redeemed not found")
    }
  }

  companion object {

    private const val AMOUNT = "amount"
    private const val PENDING_AMOUNT = "pending_amount"
    private const val COMPLETED_INVITES = "completed_invites"
    private const val RECEIVED_AMOUNT = "received_amount"
    private const val MAX_AMOUNT = "max_amount"
    private const val AVAILABLE = "available"
    private const val CURRENCY = "currency"
    private const val IS_REDEEMED = "is_redeemed"

    fun newInstance(amount: BigDecimal, pendingAmount: BigDecimal, currency: String,
                    completed: Int, receivedAmount: BigDecimal, maxAmount: BigDecimal,
                    available: Int, isRedeemed: Boolean): ReferralsFragment {
      val bundle = Bundle()
      bundle.putSerializable(AMOUNT, amount)
      bundle.putSerializable(PENDING_AMOUNT, pendingAmount)
      bundle.putString(CURRENCY, currency)
      bundle.putInt(COMPLETED_INVITES, completed)
      bundle.putSerializable(RECEIVED_AMOUNT, receivedAmount)
      bundle.putSerializable(MAX_AMOUNT, maxAmount)
      bundle.putInt(AVAILABLE, available)
      bundle.putBoolean(IS_REDEEMED, isRedeemed)
      val fragment = ReferralsFragment()
      fragment.arguments = bundle
      return fragment
    }
  }
}
