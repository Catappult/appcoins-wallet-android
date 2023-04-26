package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import java.math.BigDecimal
import java.util.*

data class GamificationInfo(val currentLevel: Int, val totalSpend: BigDecimal,
                            val totalEarned: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue?,
                            val nextLevelAmount: BigDecimal?,
                            val levels: List<Levels.Level>,
                            val updateDate: Date?,
                            val status: Status,
                            val fromCache: Boolean = false) {

  constructor(status: Status, fromCache: Boolean) : this(0, BigDecimal.ZERO, null, null,
      emptyList(), null, status, fromCache)

}
