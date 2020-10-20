package com.asfoundation.wallet.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_link_id")
data class TransactionLinkIdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val transactionId: String,
    val linkTransactionId: String
)