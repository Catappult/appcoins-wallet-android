package com.asfoundation.wallet.repository.entity

import androidx.room.Entity

/**
 * Entity that holds the last time a wallet was updated
 */
@Entity(primaryKeys = ["wallet"])
data class LastUpdatedWalletEntity(val wallet: String,
                                   val transactionsUpdateTimestamp: Long)