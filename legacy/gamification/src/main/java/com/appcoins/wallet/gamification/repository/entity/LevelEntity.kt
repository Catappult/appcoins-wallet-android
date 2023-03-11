package com.appcoins.wallet.gamification.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity
data class LevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val amount: BigDecimal,
    val bonus: Double,
    val level: Int
)