package com.asfoundation.wallet.ui.gamification

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import com.appcoins.wallet.gamification.LevelViewModel
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.current_level_layout.view.*
import java.math.BigDecimal


class CurrentLevelViewHolder(itemView: View,
                             private val context: Context,
                             private val amountSpent: BigDecimal,
                             private val currentLevel: Int,
                             private val uiEventListener: PublishSubject<Boolean>) :
    LevelsViewHolder(itemView) {

  override fun bind(level: LevelViewModel) {
    handleToogleButton()
  }

  private fun handleToogleButton() {
    if (currentLevel != 0) {
      val arrow = itemView.toogle_button.compoundDrawablesRelative[2]
      itemView.toogle_button.setOnCheckedChangeListener { _, isChecked ->
        uiEventListener.onNext(isChecked)
        arrow?.let {
          ObjectAnimator.ofInt(it, "level", 0, 10000)
              .start()
        }
      }
    } else {
      itemView.toogle_button.visibility = View.GONE
    }
  }


}