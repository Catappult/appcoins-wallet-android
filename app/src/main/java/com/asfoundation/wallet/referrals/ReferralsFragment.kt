package com.asfoundation.wallet.referrals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.invited_friends_animation_list.*
import kotlinx.android.synthetic.main.referrals_layout.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

class ReferralsFragment : DaggerFragment(), ReferralsView {

  private lateinit var presenter: ReferralsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = ReferralsPresenter(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.referrals_layout, container, false)
  }

  override fun setupLayout() {
    val totalAvailable = completedInvites + available
    friends_invited.text = String.format("%d/%d", completedInvites, totalAvailable)
    friends_invited.visibility = VISIBLE
    number_friends_invited.text = String.format("%d/%d", completedInvites, totalAvailable)
    total_earned.text = currency.plus(receivedAmount)
    total_earned.visibility = VISIBLE
    val individualEarn = currency + amount.setScale(2, RoundingMode.FLOOR).toString()
    val totalEarn = currency + maxAmount.setScale(2, RoundingMode.FLOOR).toString()
    referral_explanation.text =
        getString(R.string.referral_dropup_menu_requirements_body, individualEarn, totalEarn)
    invitations_progress_bar.progress =
        ((100 / (completedInvites.toDouble() + available.toDouble())) * completedInvites).roundToInt()
    setFriendsAnimations(completedInvites, completedInvites + available)
  }

  private fun setFriendsAnimations(invited: Int, totalInvitations: Int) {
    val friendsAnimation =
        arrayOf(friend_animation_1, friend_animation_2, friend_animation_3, friend_animation_4,
            friend_animation_5)

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
    if (arguments!!.containsKey(RECEIVED_AMOUNT)) {
      arguments!!.getSerializable(RECEIVED_AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Received amount not found")
    }
  }

  private val maxAmount: BigDecimal by lazy {
    if (arguments!!.containsKey(MAX_AMOUNT)) {
      arguments!!.getSerializable(MAX_AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Max amount not found")
    }
  }

  private val amount: BigDecimal by lazy {
    if (arguments!!.containsKey(AMOUNT)) {
      arguments!!.getSerializable(AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Amount not found")
    }
  }

  private val currency: String by lazy {
    if (arguments!!.containsKey(CURRENCY)) {
      arguments!!.getString(CURRENCY)
    } else {
      throw IllegalArgumentException("Currency not found")
    }
  }

  private val completedInvites: Int by lazy {
    if (arguments!!.containsKey(COMPLETED_INVITES)) {
      arguments!!.getInt(COMPLETED_INVITES)
    } else {
      throw IllegalArgumentException("Completed not found")
    }
  }

  private val available: Int by lazy {
    if (arguments!!.containsKey(AVAILABLE)) {
      arguments!!.getInt(AVAILABLE)
    } else {
      throw IllegalArgumentException("available not found")
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

    fun newInstance(amount: BigDecimal, pendingAmount: BigDecimal, currency: String,
                    completed: Int, receivedAmount: BigDecimal, maxAmount: BigDecimal,
                    available: Int): ReferralsFragment {
      val bundle = Bundle()
      bundle.putSerializable(AMOUNT, amount)
      bundle.putSerializable(PENDING_AMOUNT, pendingAmount)
      bundle.putString(CURRENCY, currency)
      bundle.putInt(COMPLETED_INVITES, completed)
      bundle.putSerializable(RECEIVED_AMOUNT, receivedAmount)
      bundle.putSerializable(MAX_AMOUNT, maxAmount)
      bundle.putInt(AVAILABLE, available)
      val fragment = ReferralsFragment()
      fragment.arguments = bundle
      return fragment
    }
  }
}
