package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.gamification.LevelModel
import com.appcoins.wallet.gamification.LevelModel.LevelType
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class LevelsAdapter(private val context: Context,
                    private val hiddenLevels: List<LevelModel>,
                    shownLevels: List<LevelModel>,
                    private val amountSpent: BigDecimal, private val currentLevel: Int,
                    private val nextLevelAmount: BigDecimal,
                    private val currencyFormatUtils: CurrencyFormatUtils,
                    private val mapper: GamificationMapper,
                    private val uiEventListener: PublishSubject<Boolean>) :
    RecyclerView.Adapter<LevelsViewHolder>() {

  private var activeLevelList: MutableList<LevelModel> = shownLevels.toMutableList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelsViewHolder {
    return when (viewType) {
      REACHED_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.reached_level_layout, parent, false)
        LevelReachedViewHolder(layout, mapper)
      }
      CURRENT_LEVEL_VIEW_TYPE -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.current_level_layout, parent, false)
        CurrentLevelViewHolder(layout, context, amountSpent, nextLevelAmount, currencyFormatUtils,
            mapper, uiEventListener)
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

  fun toggleReachedLevels(show: Boolean) {
    if (show) {
      if (currentLevel != 0) {
        for (level in hiddenLevels.reversed()) {
          activeLevelList.add(0, level)
        }
        notifyItemRangeInserted(0, currentLevel)
      }
    } else {
      activeLevelList.removeAll { it.levelType == LevelType.REACHED }
      notifyDataSetChanged()
    }
  }

  companion object {
    private const val CURRENT_LEVEL_VIEW_TYPE = 0
    private const val REACHED_VIEW_TYPE = 1
    private const val UNREACHED_VIEW_TYPE = 2
  }
}

