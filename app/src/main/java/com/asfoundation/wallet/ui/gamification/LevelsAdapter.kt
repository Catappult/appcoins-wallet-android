package com.asfoundation.wallet.ui.gamification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject

class LevelsAdapter(
    private val currencyFormatUtils: CurrencyFormatUtils,
    private val mapper: GamificationMapper,
    private val uiEventListener: PublishSubject<Pair<String, Boolean>>
) : RecyclerView.Adapter<LevelsViewHolder>() {

  /**
   * these fields are meant to be properly initialized before onCreateViewHolder is called.
   * @see setLevelsContent
   */
  private val hiddenLevels: MutableList<LevelItem> = mutableListOf()
  private val activeLevelList: MutableList<LevelItem> = mutableListOf()

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
        CurrentLevelViewHolder(layout, currencyFormatUtils, mapper, uiEventListener)
      }
      else -> {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.unreached_level_layout, parent, false)
        UnreachedLevelViewHolder(layout, currencyFormatUtils)
      }
    }
  }

  override fun getItemCount() = activeLevelList.size

  override fun onBindViewHolder(holder: LevelsViewHolder, position: Int) {
    holder.bind(activeLevelList[position])
  }

  override fun getItemViewType(position: Int): Int {
    return when (activeLevelList[position]) {
      is ReachedLevelItem -> REACHED_VIEW_TYPE
      is CurrentLevelItem -> CURRENT_LEVEL_VIEW_TYPE
      else -> UNREACHED_VIEW_TYPE
    }
  }

  /**
   * set the content to display regarding the several levels
   * with this one can update the view holders several times
   */
  fun setLevelsContent(hiddenLevels: List<LevelItem>, shownLevels: List<LevelItem>) {
    this.hiddenLevels.clear()
    this.hiddenLevels.addAll(hiddenLevels)
    if (reachedLevelsShown(activeLevelList)) {
      activeLevelList.clear()
      activeLevelList.addAll(hiddenLevels)
      activeLevelList.addAll(shownLevels)
    } else {
      activeLevelList.clear()
      activeLevelList.addAll(shownLevels)
    }
    notifyDataSetChanged()
  }

  private fun reachedLevelsShown(activeLevelList: MutableList<LevelItem>) =
      activeLevelList.any { it is ReachedLevelItem }

  fun toggleReachedLevels(show: Boolean) {
    if (show) {
      if (activeLevelList.isNotEmpty() && activeLevelList[0] is CurrentLevelItem) {
        for (level in hiddenLevels.reversed()) {
          activeLevelList.add(0, level)
        }
        notifyItemRangeInserted(0, hiddenLevels.size)
      }
    } else {
      activeLevelList.removeAll { it is ReachedLevelItem }
      notifyDataSetChanged()
    }
  }

  companion object {
    private const val CURRENT_LEVEL_VIEW_TYPE = 0
    private const val REACHED_VIEW_TYPE = 1
    private const val UNREACHED_VIEW_TYPE = 2
  }
}

