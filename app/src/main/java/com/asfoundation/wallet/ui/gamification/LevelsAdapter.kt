package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.gamification.LevelViewModel
import com.appcoins.wallet.gamification.LevelViewModel.LevelType
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class LevelsAdapter(private val context: Context, private val levels: List<LevelViewModel>,
                    private val amountSpent: BigDecimal, private val currentLevel: Int,
                    private val nextLevelAmount: BigDecimal,
                    private val currencyFormatUtils: CurrencyFormatUtils,
                    private val uiEventListener: PublishSubject<Boolean>) :
    RecyclerView.Adapter<LevelsViewHolder>() {

  private var activeLevelList: MutableList<LevelViewModel> = levels.toMutableList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelsViewHolder {
    return when (viewType) {
      REACHED_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.reached_level_layout, parent, false)
        LevelReachedViewHolder(layout, context, currencyFormatUtils)
      }
      CURRENT_LEVEL_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.current_level_layout, parent, false)
        CurrentLevelViewHolder(layout, context, amountSpent, nextLevelAmount,
            currencyFormatUtils, uiEventListener)
      }
      else -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.unreached_level_layout, parent, false)
        UnreachedLevelViewHolder(layout, context, currencyFormatUtils)
      }
    }
  }

  override fun getItemCount() = activeLevelList.size

  override fun onBindViewHolder(holder: LevelsViewHolder, position: Int) {
    holder.bind(activeLevelList[position])
  }

  override fun getItemViewType(position: Int): Int {
    return when (activeLevelList[position].levelType) {
      LevelType.REACHED -> REACHED_VIEW_TYPE
      LevelType.CURRENT -> CURRENT_LEVEL_VIEW_TYPE
      LevelType.UNREACHED -> UNREACHED_VIEW_TYPE
    }
  }

  fun toogleReachedLevels(hide: Boolean) {
    if (hide) {
      activeLevelList.removeAll { it.levelType == LevelType.REACHED }
      notifyItemRangeRemoved(0, currentLevel)
    } else if (currentLevel != 0) {
      for (i in currentLevel - 1 downTo 0) {
        activeLevelList.add(0, levels[i])
      }
      notifyItemRangeInserted(0, currentLevel)
    }
  }

  companion object {
    private const val CURRENT_LEVEL_VIEW_TYPE = 0
    private const val REACHED_VIEW_TYPE = 1
    private const val UNREACHED_VIEW_TYPE = 2
  }
}

