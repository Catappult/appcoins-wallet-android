package com.asfoundation.wallet.referrals

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.extensions.scaleToString
import com.asf.wallet.databinding.InviteFriendsFragmentLayoutBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class InviteFriendsFragment : BasePageViewFragment(), InviteFriendsFragmentView {

  @Inject
  lateinit var referralInteractor: ReferralInteractorContract

  private lateinit var presenter: InviteFriendsFragmentPresenter
  private var activity: InviteFriendsActivityView? = null
  private lateinit var referralsBottomSheet: BottomSheetBehavior<View>

  private val binding by viewBinding(InviteFriendsFragmentLayoutBinding::bind)

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
        BottomSheetBehavior.from(binding.bottomSheetFragmentContainer)
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
        binding.backgroundFadeAnimation.progress = slideOffset
      }
    })
  }

  private fun setTextValues() {
    binding.referralDescription.text =
        getString(R.string.referral_view_verified_body,
            currency + amount.scaleToString(2))
    binding.referralNotificationCard.notificationTitle.text =
        getString(R.string.referral_notification_bonus_pending_title,
            currency + pendingAmount.scaleToString(2))
  }

  override fun shareLinkClick(): Observable<Any> {
    return RxView.clicks(binding.shareInviteButton)
  }


  override fun showShare() {
    activity?.showShare(link)
  }

  override fun showNotificationCard(pendingAmount: BigDecimal, symbol: String,
                                    icon: Int?) {
    if (pendingAmount.toDouble() > 0) {
      icon?.let { binding.referralNotificationCard.notificationImage.setImageResource(icon) }
      binding.referralNotificationCard.notificationTitle.text = getString(R.string.referral_notification_bonus_pending_title,
          "$symbol${pendingAmount.scaleToString(2)}")
      binding.referralNotificationCard.root.visibility = VISIBLE
    } else {
      binding.referralNotificationCard.root.visibility = GONE
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
    if (requireArguments().containsKey(RECEIVED_AMOUNT)) {
      requireArguments().getSerializable(RECEIVED_AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Received amount not found")
    }
  }

  private val maxAmount: BigDecimal by lazy {
    if (requireArguments().containsKey(MAX_AMOUNT)) {
      requireArguments().getSerializable(MAX_AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Max amount not found")
    }
  }

  private val amount: BigDecimal by lazy {
    if (requireArguments().containsKey(AMOUNT)) {
      requireArguments().getSerializable(AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Amount not found")
    }
  }

  private val pendingAmount: BigDecimal by lazy {
    if (requireArguments().containsKey(PENDING_AMOUNT)) {
      requireArguments().getSerializable(PENDING_AMOUNT) as BigDecimal
    } else {
      throw IllegalArgumentException("Pending amount not found")
    }
  }

  private val currency: String by lazy {
    if (requireArguments().containsKey(CURRENCY)) {
      requireArguments().getString(CURRENCY, "")
    } else {
      throw IllegalArgumentException("Currency not found")
    }
  }

  private val link: String by lazy {
    if (requireArguments().containsKey(LINK)) {
      requireArguments().getString(LINK, "")
    } else {
      throw IllegalArgumentException("link not found")
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
