package com.asfoundation.wallet.promotions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.asf.wallet.R
import com.asfoundation.wallet.ui.gamification.CurrentLevelInfo
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.gamification_card.*
import kotlinx.android.synthetic.main.legacy_gamification_card.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import kotlinx.android.synthetic.main.promotions_fragment_view.*
import kotlinx.android.synthetic.main.promotions_fragment_view.referrals_card
import kotlinx.android.synthetic.main.referral_card_layout.*
import kotlinx.android.synthetic.main.rewards_progress_bar.*
import java.math.BigDecimal
import java.text.DecimalFormat
import javax.inject.Inject

class PromotionsFragment : DaggerFragment(), PromotionsView {

  @Inject
  lateinit var gamification: GamificationInteractor

  @Inject
  lateinit var promotionsInteractor: PromotionsInteractorContract

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var mapper: GamificationMapper
  private lateinit var activityView: PromotionsActivityView
  private var step = 100
  private lateinit var presenter: PromotionsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        PromotionsPresenter(this, activityView, promotionsInteractor, mapper, CompositeDisposable(),
            Schedulers.io(), AndroidSchedulers.mainThread(), formatter)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is PromotionsActivityView) { PromotionsFragment::class.java.simpleName + " needs to be attached to a " + PromotionsActivityView::class.java.simpleName }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.promotions_fragment_view, container, false)
  }

  override fun setLegacyLevelIcons() {
    for (i in 0..4) {
      legacy_gamification_progress_bar.setLevelIcons(i)
    }
  }

  override fun setLegacyStaringLevel(userStatus: UserRewardsStatus) {
    progress_bar.progress = userStatus.lastShownLevel * (100 / (userStatus.bonus.size - 1))
    for (i in 0..userStatus.lastShownLevel) {
      legacy_gamification_progress_bar.showPreviousLevelIcons(i, i < userStatus.lastShownLevel)
    }
  }

  override fun setLevelInformation(userStatus: UserRewardsStatus, legacy: Boolean) {
    val earnUpTo = getString(R.string.promotions_gamification_card_title_variable,
        formatter.formatGamificationValues(BigDecimal(userStatus.maxBonus)))
    if (legacy) {
      updateLegacyLevel(userStatus, earnUpTo)
    } else {
      gamification_title.text = earnUpTo
      planet_subtitle.text = getString(R.string.gamif_card_body,
          formatter.formatGamificationValues(userStatus.toNextLevelAmount))
    }
  }

  private fun updateLegacyLevel(userStatus: UserRewardsStatus, earnUpTo: String) {
    legacy_gamification_title.text = earnUpTo
    if (userStatus.bonus.size != 1) {
      step = 100 / (userStatus.bonus.size - 1)
    }
    legacy_gamification_progress_bar.animateProgress(userStatus.lastShownLevel, userStatus.level,
        step)
    for (value in userStatus.bonus) {
      val level = userStatus.bonus.indexOf(value)
      val bonusLabel = R.string.gamification_how_table_b2
      legacy_gamification_progress_bar.setLevelBonus(level,
          getString(bonusLabel, legacy_gamification_progress_bar.formatLevelInfo(value)))
    }
  }

  override fun showLoading() {
    promotions_progress_bar.visibility = VISIBLE
  }

  override fun showReferralUpdate(show: Boolean) {
    if (show) {
      if (referal_update.visibility == INVISIBLE) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_animation)
        animation.duration = 750
        referal_update.visibility = VISIBLE
        referal_update.startAnimation(animation)
      }
    } else if (referal_update.visibility == VISIBLE) {
      referal_update.startAnimation(
          AnimationUtils.loadAnimation(context, R.anim.fade_out_animation))
      referal_update.visibility = INVISIBLE
    }
  }

  override fun showGamificationUpdate(show: Boolean) {
    showGamificationUpdate(show, gamification_update)
  }

  override fun showLegacyGamificationUpdate(show: Boolean) {
    showGamificationUpdate(show, legacy_gamification_update)
  }

  private fun showGamificationUpdate(show: Boolean, view: View) {
    if (show) {
      if (legacy_gamification_update.visibility == INVISIBLE) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_animation)
        animation.duration = 750
        view.visibility = VISIBLE
        view.startAnimation(animation)
      }
    } else if (view.visibility == VISIBLE) {
      view.startAnimation(
          AnimationUtils.loadAnimation(context, R.anim.fade_out_animation))
      view.visibility = INVISIBLE
    }
  }

  override fun seeMoreClick() = RxView.clicks(see_more_button)

  override fun legacySeeMoreClick() = RxView.clicks(legacy_see_more_button)

  override fun detailsClick() = RxView.clicks(details_button)

  override fun shareClick() = RxView.clicks(share_button)

  override fun legacyGamificationCardClick() = RxView.clicks(legacy_gamification_card)

  override fun gamificationCardClick() = RxView.clicks(gamification_card)

  override fun referralCardClick() = RxView.clicks(referrals_card)

  override fun retryClick() = RxView.clicks(retry_button)

  override fun showReferralCard() {
    no_promotions.visibility = GONE
    no_network.visibility = GONE
    promotions_container.visibility = VISIBLE
    referrals_card.visibility = VISIBLE
  }

  override fun showLegacyGamificationCard() {
    no_promotions.visibility = GONE
    no_network.visibility = GONE
    promotions_container.visibility = VISIBLE
    legacy_gamification_card.visibility = VISIBLE
    gamification_card.visibility = GONE
  }

  override fun showGamificationCard(currentLevelInfo: CurrentLevelInfo, bonus: Double) {
    no_promotions.visibility = GONE
    no_network.visibility = GONE
    promotions_container.visibility = VISIBLE
    gamification_card.visibility = VISIBLE
    legacy_gamification_card.visibility = GONE
    planet.setImageDrawable(currentLevelInfo.planet)
    current_level_bonus.background = mapper.getOvalBackground(currentLevelInfo.levelColor)
    val df = DecimalFormat("###.#")
    current_level_bonus.text = context?.getString(R.string.gamif_bonus, df.format(bonus))
    planet_title.text = currentLevelInfo.title
  }

  override fun showNetworkErrorView() {
    no_promotions.visibility = GONE
    no_network.visibility = VISIBLE
    retry_button.visibility = VISIBLE
    retry_animation.visibility = GONE
    promotions_container.visibility = GONE
  }

  override fun showNoPromotionsScreen() {
    no_network.visibility = GONE
    retry_animation.visibility = GONE
    promotions_container.visibility = GONE
    no_promotions.visibility = VISIBLE
  }

  override fun showRetryAnimation() {
    retry_button.visibility = INVISIBLE
    retry_animation.visibility = VISIBLE
  }

  override fun setReferralBonus(bonus: String, currency: String) {
    promotions_title.text = getString(R.string.promotions_referral_card_title,
        currency + bonus)
  }

  override fun toggleShareAvailability(validated: Boolean) {
    share_button.isEnabled = validated
  }

  override fun hideLoading() {
    promotions_progress_bar.visibility = INVISIBLE
  }

  override fun onResume() {
    presenter.present()
    super.onResume()
  }

  override fun onPause() {
    presenter.stop()
    super.onPause()
  }

  companion object {
    fun newInstance() = PromotionsFragment()
  }
}
