package com.asfoundation.wallet.referrals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.invited_friends_animation_list.*
import kotlinx.android.synthetic.main.referrals_layout.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class ReferralsFragment : DaggerFragment(), ReferralsView {

  private lateinit var presenter: ReferralsPresenter
  @Inject
  lateinit var referralInteractor: ReferralInteractorContract

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = ReferralsPresenter(this, referralInteractor, CompositeDisposable(),
        AndroidSchedulers.mainThread(),
        Schedulers.io())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.referrals_layout, container, false)
  }

  override fun setupLayout(invited: Int, currency: String, receivedAmount: BigDecimal,
                           amount: BigDecimal,
                           maxAmount: BigDecimal, available: Int) {
    invited_friends_skeleton.visibility = GONE
    received_amount_skeleton.visibility = GONE
    val totalAvailable = invited + available
    friends_invited.text = String.format("%d/%d", invited, totalAvailable)
    friends_invited.visibility = VISIBLE
    total_earned.text = currency.plus(receivedAmount)
    total_earned.visibility = VISIBLE
    val individualEarn = currency + amount.setScale(2, RoundingMode.FLOOR).toString()
    val totalEarn = currency + maxAmount.setScale(2, RoundingMode.FLOOR).toString()
    referral_explanation.text =
        getString(R.string.referral_dropup_menu_requirements_body, individualEarn, totalEarn)
    invitations_progress_bar.progress = (100 / (invited + available)) * invited
    setFriendsAnimations(invited, invited + available)
  }

  private fun setFriendsAnimations(invited: Int, totalInvitations: Int) {
    val friendsAnimation =
        arrayOf(friend_animation_1, friend_animation_2, friend_animation_3, friend_animation_4,
            friend_animation_5)
    if (invited != 0) {
      for (i in friendsAnimation.indices) {
        if (i < invited) friendsAnimation[i].setAnimation(R.raw.invited_user_animation)
        if (i >= totalInvitations) friendsAnimation[i].visibility = GONE
      }
    } else if (totalInvitations < friendsAnimation.size) {
      for (i in totalInvitations until friendsAnimation.size) {
        friendsAnimation[i].visibility = GONE
      }
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
