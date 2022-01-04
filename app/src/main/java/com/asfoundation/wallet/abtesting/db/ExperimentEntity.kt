package com.asfoundation.wallet.abtesting.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experiment")
data class ExperimentEntity(@PrimaryKey val experimentName: String, val requestTime: Long,
                            val assignment: String?, val payload: String?,
                            val partOfExperiment: Boolean, val experimentOver: Boolean)
