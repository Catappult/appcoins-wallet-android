package com.appcoins.wallet.gamification.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class LevelsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val status: LevelsResponse.Status,
    @ColumnInfo(name = "update_date")
    val updateDate: Date?
)