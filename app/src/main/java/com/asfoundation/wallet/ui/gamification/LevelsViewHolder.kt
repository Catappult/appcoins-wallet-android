package com.asfoundation.wallet.ui.gamification

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class LevelsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  abstract fun bind(level: LevelItem)
}
