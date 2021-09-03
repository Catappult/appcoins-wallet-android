package com.asfoundation.wallet.referrals

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.util.scaleToString
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.invite_friends_fragment_layout.*
import kotlinx.android.synthetic.main.referral_notification_card.*
import java.math.BigDecimal
import javax.inject.Inject

class InviteFriendsFragment : BasePageViewFragment(), InviteFriendsFragmentView {

  @Inject
  lateinit var referralInteractor: ReferralInteractorContract

  private lateinit var presenter: InviteFriendsFragmentPresenter
  private var activity: InviteFriendsActivityView? = null
  private lateinit var referralsBottomSheet: BottomSheetBehavior<View>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        InviteFriendsFragmentPresenter(this, activity, CompositeDisposable(), referralInteractor)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is InviteFriendsActivityView) { InviteFriendsFragment::class.java.simpleName + " needs to be attached to a " + InviteFriendsActivity::class.java.simpleName }
    activity = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    referralsBottomSheet =
        BottomSheetBehavior.from(bottom_sheet_fragment_container)
    animateBackgroundFade()
    setTextValues()
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    childFragmentManager.beginTransaction()
        .replace(R.id.bottom_sheet_fragment_container,
            ReferralsFragment.newInstance(amount, pendingAmount, currency, completedInvites,
                receivedAmount, maxAmount, available, isRedeemed))
        .commit()
    return inflater.inflate(R.layout.invite_friends_fragment_layout, container, false)
  }

  private fun animateBackgroundFade() {
    referralsBottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        background_fade_animation?.progress = slideOffset
      }
    })
  }

  private fun setTextValues() {
    referral_description.text =
        getString(R.string.referral_view_verified_body,
            currency + amount.scaleToString(2))
    notification_title.text =
        getString(R.string.referral_notification_bonus_pending_title,
            currency + pendingAmount.scaleToString(2))
  }

  override fun shareLinkClick(): Observable<Any> {
    return RxView.clicks(share_invite_button)
  }


  override fun showShare() {
    activity?.showShare(link)
  }

  override fun showNotificationCard(pendingAmount: BigDecimal, symbol: String,
                                    icon: Int?) {
    if (pendingAmount.toDouble() > 0) {
      icon?.let { notification_image.setImageResource(icon) }
      notification_title.text = getString(R.string.referral_notification_bonus_pending_title,
          "$symbol${pendingAmount.scaleToString(2)}")
      referral_notification_card.visibility = VISIBLE
    } else {
      referral_notification_card.visibility = GONE
    }
  }

  override fun changeBottomSheetState() {
    if (referralsBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
      referralsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    } else if (referralsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
      referralsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
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

  private val pendingAmount: BigDecimal by lazy {
    if (arguments!!.containsKey(PENDING_AMOUNT)) {
      arguments!!.getSerializable(PENDING_AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Pending amount not found")
    }
  }

  private val currency: String by lazy {
    if (arguments!!.containsKey(CURRENCY)) {
      arguments!!.getString(CURRENCY, "")
    } else {
      throw IllegalArgumentException("Currency not found")
    }
  }

  private val link: String by lazy {
    if (arguments!!.containsKey(LINK)) {
      arguments!!.getString(LINK, "")
    } else {
      throw IllegalArgumentException("link not found")
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

  private val isRedeemed: Boolean by lazy {
    if (arguments!!.containsKey(IS_REDEEMED)) {
      arguments!!.getBoolean(IS_REDEEMED)
    } else {
      throw IllegalArgumentException("is redeemed not found")
    }
  }

  companion object {

    private const val AMOUNT = "amount"
    private const val PENDING_AMOUNT = "pending_amount"
    private const val LINK = "link"
    private const val COMPLETED_INVITES = "completed_invites"
    private const val RECEIVED_AMOUNT = "received_amount"
    private const val MAX_AMOUNT = "max_amount"
    private const val AVAILABLE = "available"
    private const val CURRENCY = "currency"
    private const val IS_REDEEMED = "is_redeemed"

    fun newInstance(amount: BigDecimal, pendingAmount: BigDecimal, currency: String, link: String?,
                    completed: Int, receivedAmount: BigDecimal, maxAmount: BigDecimal,
                    available: Int, isRedeemed: Boolean): InviteFriendsFragment {
      val bundle = Bundle().apply {
        putSerializable(AMOUNT, amount)
        putSerializable(PENDING_AMOUNT, pendingAmount)
        putString(CURRENCY, currency)
        putString(LINK, link)
        putInt(COMPLETED_INVITES, completed)
        putSerializable(RECEIVED_AMOUNT, receivedAmount)
        putSerializable(MAX_AMOUNT, maxAmount)
        putInt(AVAILABLE, available)
        putBoolean(IS_REDEEMED, isRedeemed)
      }
      return InviteFriendsFragment().apply {
        arguments = bundle
      }
    }
  }
}
