package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.gamification.repository.Levels
import com.asf.wallet.R
import java.math.BigDecimal

class LevelsAdapter(private val context: Context, private val levels: List<Levels.Level>,
                    private val amountSpent: BigDecimal, private val currentLevel: Int) :
    RecyclerView.Adapter<LevelsViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelsViewHolder {
    return when (viewType) {
      REACHED_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.reached_level_layout, parent, false)
        LevelReachedViewHolder(layout, context)
      }
      CURRENT_LEVEL_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.current_level_layout, parent, false)
        CurrentLevelViewHolder(layout, context, amountSpent)
      }
      else -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.unreached_level_layout, parent, false)
        UnreachedLevelViewHolder(layout, context)
      }
    }
  }

  override fun getItemCount() = levels.size

  override fun onBindViewHolder(holder: LevelsViewHolder, position: Int) {
    holder.bind(levels[position])
  }

  override fun getItemViewType(position: Int): Int {
    return when {
      levels[position].level < currentLevel -> REACHED_VIEW_TYPE
      levels[position].level == currentLevel -> CURRENT_LEVEL_VIEW_TYPE
      else -> UNREACHED_VIEW_TYPE
    }
  }

  companion object {
    private const val CURRENT_LEVEL_VIEW_TYPE = 0
    private const val REACHED_VIEW_TYPE = 1
    private const val UNREACHED_VIEW_TYPE = 2
  }
}
