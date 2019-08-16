package com.asfoundation.wallet.promotions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.gamification_card_layout.*
import kotlinx.android.synthetic.main.rewards_progress_bar.*
import javax.inject.Inject

class PromotionsFragment : DaggerFragment(), PromotionsView {

  @Inject
  lateinit var gamification: GamificationInteractor
  private var step = 100


  private lateinit var presenter: PromotionsPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = PromotionsPresenter(this, gamification, CompositeDisposable(), Schedulers.io(),
        AndroidSchedulers.mainThread())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.promotions_fragment_view, container, false)
  }

  override fun setupLayout() {
    for (i in 0..4) {
      gamification_progress_bar.setLevelIcons(i)
    }
  }

  override fun setStaringLevel(userStatus: UserRewardsStatus) {
    progress_bar.progress = userStatus.lastShownLevel * (100 / (userStatus.bonus.size - 1))
    for (i in 0..userStatus.lastShownLevel) {
      gamification_progress_bar.showPreviousLevelIcons(i, i < userStatus.lastShownLevel)
    }
  }

  override fun updateLevel(userStatus: UserRewardsStatus) {
    if (userStatus.bonus.size != 1) {
      step = 100 / (userStatus.bonus.size - 1)
    }

    gamification_progress_bar.animateProgress(userStatus.lastShownLevel, userStatus.level, step)

    for (value in userStatus.bonus) {
      val level = userStatus.bonus.indexOf(value)
      val isCurrentLevel = level == userStatus.level
      val bonusLabel = if (isCurrentLevel) {
        R.string.gamification_level_bonus
      } else {
        R.string.gamification_how_table_b2
      }
      gamification_progress_bar.setLevelBonus(level,
          getString(bonusLabel, gamification_progress_bar.formatLevelInfo(value)))
    }
  }
}
