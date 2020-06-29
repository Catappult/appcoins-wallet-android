package com.asfoundation.wallet.ui.gamification

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.gamification.LevelViewModel

abstract class LevelsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  abstract fun bind(level: LevelViewModel)
}
