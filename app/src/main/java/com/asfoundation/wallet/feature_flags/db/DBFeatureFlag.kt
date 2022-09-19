package com.asfoundation.wallet.feature_flags.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_flag")
data class DBFeatureFlag(
  @PrimaryKey
  @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
  val flagId: String,
  @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
  val variant: String,
  @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
  val payload: String,
)
