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
                    private val currencyFormatUtils: CurrencyFormatUtils,
                    private val mapper: GamificationMapper,
                    private val uiEventListener: PublishSubject<Pair<String, Boolean>>) :
    RecyclerView.Adapter<LevelsViewHolder>() {

  /**
   * these fields are meant to be initialized before onCreateViewHolder is called.
   * @see setLevelsContent
   */
  private lateinit var hiddenLevels: List<LevelModel>
  private lateinit var shownLevels: List<LevelModel>
  private lateinit var amountSpent: BigDecimal
  private var currentLevel: Int = -1
  private var nextLevelAmount: BigDecimal? = null
  private val activeLevelList: MutableList<LevelModel> = mutableListOf()

  private var reachedLevelsToggled = false

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
        CurrentLevelViewHolder(layout, context, currencyFormatUtils, mapper, uiEventListener)
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
    if (holder is CurrentLevelViewHolder) holder.setAmounts(amountSpent, nextLevelAmount)
    holder.bind(activeLevelList[position])
  }

  override fun getItemViewType(position: Int): Int {
    return when (activeLevelList[position].levelType) {
      LevelType.REACHED -> REACHED_VIEW_TYPE
      LevelType.CURRENT -> CURRENT_LEVEL_VIEW_TYPE
      LevelType.UNREACHED -> UNREACHED_VIEW_TYPE
    }
  }

  /**
   * set the content to display regarding the several levels
   * with this one can update the view holders several times
   */
  fun setLevelsContent(_hiddenLevels: List<LevelModel>, _shownLevels: List<LevelModel>,
                       _amountSpent: BigDecimal, _currentLevel: Int,
                       _nextLevelAmount: BigDecimal?) {
    activeLevelList.clear()
    hiddenLevels = _hiddenLevels
    shownLevels = _shownLevels
    amountSpent = _amountSpent
    currentLevel = _currentLevel
    nextLevelAmount = _nextLevelAmount
    if (reachedLevelsToggled) {
      activeLevelList.addAll(hiddenLevels)
      activeLevelList.addAll(shownLevels)
    } else activeLevelList.addAll(shownLevels)
    notifyDataSetChanged()
  }

  fun toggleReachedLevels(show: Boolean) {
    if (show) {
      reachedLevelsToggled = true
      if (currentLevel != 0) {
        for (level in hiddenLevels.reversed()) {
          activeLevelList.add(0, level)
        }
        notifyItemRangeInserted(0, currentLevel)
      }
    } else {
      reachedLevelsToggled = false
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

